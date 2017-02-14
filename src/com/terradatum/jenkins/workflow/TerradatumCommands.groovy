#!/usr/bin/env groovy
package com.terradatum.jenkins.workflow

import com.cloudbees.groovy.cps.NonCPS
import groovy.json.JsonSlurper
import groovy.json.internal.LazyMap
import groovy.util.slurpersupport.NodeChild
import jenkins.model.Jenkins
/*
 * version processing
 */

// blocking call to get version, increment, and return
// persists current version in "${path to Jenkins full name}/currentVersion" file
// if a version is passed in, and is greater than the persisted version, then it overrides
// the persisted version and becomes the new version.
/**
 * Created by rbellamy on 8/15/16.
 *
 * When working on this script using IntelliJ, you'll want to run the following
 * commands from the command line:
 * 1. mvn dependency:get -Dartifact=com.cloudbees:groovy-cps:1.9
 * 2. mvn dependency:get -Dartifact=com.github.zafarkhaja:java-semver:0.10-SNAPSHOT
 * 3. mvn dependency:get -Dartifact=org.6wind.jenkins:lockable-resources:1.10
 * 4. mvn dependency:get -Dartifact=org.jenkins-ci.plugins.workflow:workflow-api:2.1
 *
 * And then add them to the module as 'Maven' libraries.
 */

static String getPathFromJenkinsFullName(String fullName) {
  Jenkins.instance.getItemByFullName(fullName).rootDir
}

def removeTrailingSlash(String myString) {
  if (myString.endsWith("/")) {
    return myString.substring(0, myString.length() - 1)
  }
  return myString
}

def getNexusLatestVersion(String repo, String artifact) {
  String latest = getNexusVersions(repo, artifact).latest
  return Version.valueOf(latest)
}

def getNexusReleaseVersion(String repo, String artifact) {
  String release = getNexusVersions(repo, artifact).release
  return Version.valueOf(release)
}

def getNexusVersions(String repo, String artifact) {
  artifact = removeTrailingSlash(artifact)

  def metadataUrl = "${repo}/${artifact}/maven-metadata.xml"
  try {
    def modelMetadata = new XmlSlurper().parse(metadataUrl)
    return modelMetadata.versioning.versions
  } catch (err) {
    echo "There was an error retrieving ${metadataUrl}: ${err}"
    return [Version.valueOf('0.0.1')]
  }
}

def getMaxNexusVersion(String repo, String project, String artifact, Version version) {
  lock("${project}/maxNexusVersion") {
    List<NodeChild> nexusVersions = getNexusVersions(repo, artifact).version.toList()
    List<Version> versions = new ArrayList<>()
    for (int i = 0; i < nexusVersions.size(); i++) {
      NodeChild nexusVersionNode = nexusVersions[i]
      try {
        if (nexusVersionNode) {
          def nexusVersion = Version.valueOf(nexusVersionNode.text())
          if (nexusVersion.majorVersion == version.majorVersion && nexusVersion.minorVersion == version.minorVersion) {
            versions.add(nexusVersion)
          }
        }
      } catch (err) {
        echo "Not valid semantic version: ${nexusVersionNode}, error: ${err}"
      }
    }
    if (versions && versions.size() > 0) {
      def maxVersion = versions.max()
      if (maxVersion.lessThan(version)) {
        return version
      } else {
        return maxVersion
      }
    } else {
      return Version.valueOf('0.0.1')
    }
  }
}

def incrementVersion(String project, VersionType versionType, Version version = null) {
  def path = "${getPathFromJenkinsFullName(project)}/currentVersion"
  def currentVersion = Version.valueOf('0.0.1')
  def nextVersion = currentVersion
  lock("${project}/currentVersion") {
    def versionString = getStringInFile(path)
    def persistedVersion = versionString ? Version.valueOf(versionString) : currentVersion
    if (version && version.lessThan(persistedVersion)) {
      currentVersion = persistedVersion
    } else if (version) {
      currentVersion = version
    }
    switch (versionType) {
      case VersionType.Major:
        nextVersion = currentVersion.incrementPatchVersion()
        break
      case VersionType.Minor:
        nextVersion = currentVersion.incrementPatchVersion()
        break
      case VersionType.Patch:
        nextVersion = currentVersion.incrementPatchVersion()
        break
      case VersionType.PreRelease:
        nextVersion = currentVersion.incrementPreReleaseVersion()
        break
      case VersionType.BuildMetadata:
        nextVersion = currentVersion.incrementBuildMetadata()
        break
    }
    setStringInFile(path, nextVersion.toString())
  }
  nextVersion
}

// TODO: wire this up for other project types.
def getProjectVersionString(ProjectType projectType) {
  def versionString = '0.0.1'
  switch (projectType) {
    case ProjectType.Maven:
      def pom = readMavenPom file: 'pom.xml'
      versionString = pom.version
      break
    case ProjectType.Sbt:
      def sbt = readFile 'build.sbt'
      def matcher = sbt =~ /version\s*:=\s*"([0-9A-Za-z.-]+)",?/
      //noinspection GroovyAssignabilityCheck
      versionString = matcher[0][1]
      break
    case ProjectType.Node:
      //noinspection GrUnresolvedAccess,GroovyAssignabilityCheck
      def packageJson = parseJsonText(readFile('package.json'))
      versionString = packageJson.version
      break
  }
  versionString
}

def getBuildMetadataVersion(Version version) {
  String commit = shell(returnStdout: true, script: 'git rev-parse --short HEAD').trim()
  version.setBuildMetadata(commit)
}

def getTagVersion(Version version) {

  // if the repo has no tags this command will fail
  // if this command fails, and there is no config version, rethrow the error
  gitVersion = shell(returnStdout: true, script: 'git tag --sort version:refname | tail -1').trim()

  if (tag == null || tag.size() == 0) {
    echo "No existing tag found. Using version: ${version}"
    return version
  }

  tag = tag.trim()

  def semanticVersion = Version.valueOf(tag)
  Version newVersion = version
  if (newVersion.compareWithBuildsTo(semanticVersion) < 0) {
    newVersion = semanticVersion
  }
  newVersion
}

def getCurrentVersion(String project) {
  def path = "${getPathFromJenkinsFullName(project)}/currentVersion"
  Version persistedVersion = Version.valueOf('0.0.1')
  lock("${project}/currentVersion") {
    def versionString = getStringInFile(path)
    if (versionString?.trim()) {
      persistedVersion = Version.valueOf(versionString)
    }
  }
  persistedVersion
}

def setCurrentVersion(String project, Version version) {
  def path = "${getPathFromJenkinsFullName(project)}/currentVersion"
  Version persistedVersion = version ?: Version.valueOf('0.0.1')
  lock("${project}/currentVersion") {
    setStringInFile(path, persistedVersion.toString())
  }
}

def updatePomXmlRevisionWithVersion(Version version) {
  if (version) {
    if (version.buildMetadata) {
      shell "find -type f -name 'pom.xml' -exec sed -i -r 's/\\\$\\{revision\\}/${version.patchVersion}\\+${version.buildMetadata}/g' \"{}\" \\;"
    } else {
      shell "find -type f -name 'pom.xml' -exec sed -i -r 's/\\\$\\{revision\\}/${version.patchVersion}/g' \"{}\" \\;"
    }
  }
}

def updateSbtDependencies(String projectPart) {
  shell "find . -type f -name '*.sbt' -exec sed -i -r 's/(${projectPart}.*[ \\t]*%[ \\t]\"[0-9.]+)[0-9]-SNAPSHOT\"/\\1\\+\"/g' \"{}\" \\;"
}

def updateBuildSbtSnapshotToVersion(Version version) {
  if (version) {
    if (version.buildMetadata) {
      shell "find . -type f -name 'build.sbt' -exec sed -i -r 's/(version[ \\t]*:=[ \\t]*\"[0-9.]+)[0-9]-SNAPSHOT\"/\\1${version.patchVersion}\\+${version.buildMetadata}\"/g' \"{}\" \\;"
    } else {
      shell "find . -type f -name 'build.sbt' -exec sed -i -r 's/(version[ \\t]*:=[ \\t]*\"[0-9.]+)[0-9]-SNAPSHOT\"/\\1${version.patchVersion}\"/g' \"{}\" \\;"
    }
  }
}

def updatePackageJsonDockerBuildVersion(String projectPart, Version version) {
  shell "find \\( -path \"./dist\" -o -path \"./node_modules\" \\) -prune -o -name \"package.json\" -exec sed -i -r 's/(${projectPart}:[0-9.]+)[0-9]-SNAPSHOT/\\1${version.patchVersion}/g' \"{}\" \\;"
}

def updatePackageJsonSnapshotWithVersion(Version version) {
  if (version) {
    if (version.buildMetadata) {
      shell "find \\( -path \"./dist\" -o -path \"./node_modules\" \\) -prune -o -name \"package.json\" -exec sed -i -r 's/(\"version\"[ \\t]*:[ \\t]*\"[0-9.]+)[0-9]-SNAPSHOT\"/\\1${version.patchVersion}\\+${version.buildMetadata}\"/g' \"{}\" \\;"
    } else {
      shell "find \\( -path \"./dist\" -o -path \"./node_modules\" \\) -prune -o -name \"package.json\" -exec sed -i -r 's/(\"version\"[ \\t]*:[ \\t]*\"[0-9.]+)[0-9]-SNAPSHOT\"/\\1${version.patchVersion}\"/g' \"{}\" \\;"
    }
  }
}

void triggerDownstreamBuild(List<String> projectPaths) {
  // The pattern to look for when deciding which downstream build to skip: >>>!aergo-common!<<<
  String skipPattern = />>>!([a-zA-Z\-]*)!<<</
  // Look commit log for the last pull - back no further! If any of the commit messages contain the "skip" pattern, then
  // the downstream build of that name will be skipped
  String pullLog = gitPullLog()
  List<String> skipList
  if (pullLog) {
    echo """>>>Evaluated git log for skiplist<<<
${pullLog}"""
    skipList = (pullLog =~ skipPattern).collect{ all, project -> project }
  }

  if (projectPaths && projectPaths.size() > 0) {
    projectPaths.each { projectPath ->
      // assumes in the form of "../aergo-common/master" or some other Jenkins path
      def project = projectPath.tokenize('/')[-2]
      stage "trigger ${project} build"
      if (!skipList || skipList.size() == 0 || !skipList.contains(project)) {
        build(job: projectPath, propagate: false, quietPeriod: 120)
      } else {
        echo "...skipping ${project} build"
      }
    }
  }
}

void gitMerge(String targetBranch, String sourceBranch) {
  sshagent(['devops_deploy_DEV']) {
    shell "git checkout ${targetBranch}"
    shell "git merge origin/${sourceBranch}"
  }
}

void gitConfig(String project) {
  shell 'ssh-keyscan -t rsa github.com >> ~/.ssh/known_hosts'
  shell 'git config user.email sysadmin@terradatum.com'
  shell 'git config user.name terradatum-automation'
  shell "git remote set-url origin git@github.com:${project}"
}

void gitTag(Version releaseVersion) {
  sshagent(['devops_deploy_DEV']) {
    shell 'git tag -d \$(git tag)'
    shell 'git fetch --tags'
    echo "New release version ${releaseVersion.normalVersion}"
    shell "git tag -fa ${releaseVersion.normalVersion} -m 'Release version ${releaseVersion.normalVersion}'"
  }
}

void gitPush(String targetBranch) {
  sshagent(['devops_deploy_DEV']) {
    shell "git push origin ${targetBranch}"
    shell "git push --tags"
  }
}

void gitCheckout(String targetBranch) {
  sshagent(['devops_deploy_DEV']) {
    shell "git checkout ${targetBranch}"
    shell 'git pull'
  }
}

void gitResetBranch() {
  shell 'git checkout -- .'
}

String gitPullLog() {
  pullLog = shell(returnStdout: true, script: 'git log ORIG_HEAD..').trim()
}

void dockerLogin() {
  String dockerLogin = shell(returnStdout: true, script: 'aws ecr get-login --region us-west-1').trim()
  shell "sudo ${dockerLogin}"
}

void deisLogin(String controller, String username, String password) {
  shell "deis login ${controller} --username=${username} --password=${password}"
}

def getEcrPassword() {
  def ecrResponse = shell "aws ecr get-login --region us-west-1"
  def ecrSplit = ecrResponse.split(/\s+/)
  ecrSplit[5]
}

/*
 * Expects for the deis environment to have been configured properly before execution
 */
void deisAppRegistry(String app) {
  shell "deis registry:set -a $app username=AWS password=${getEcrPassword()}"
}

String shell(String script, String sourceFile = '', String encoding = 'UTF-8', boolean returnStatus = false, boolean returnStdout = false) {
  if (sourceFile != '' && fileExists(sourceFile)) {
    echo "Sourcing ${sourceFile} in bash script..."
    sh(
        script: "source ${sourceFile}\n${script}\n",
        encoding: encoding,
        returnStatus: returnStatus,
        returnStdout: returnStdout
    )
  } else {
    sh(
        script: script,
        encoding: encoding,
        returnStatus: returnStatus,
        returnStdout: returnStdout
    )
  }
}

/*
 * NonCPS - non-serializable methods
 */
// read full text from file
String shell(Map args) {
  String script = ''
  String sourceFile = ''
  String encoding = 'UTF-8'
  boolean returnStatus = false
  boolean returnStdout = false
  if (args.script) {
    script = args.script as String
  }
  if (args.sourceFile) {
    sourceFile = args.sourceFile
  }
  if (args.encoding) {
    encoding = args.encoding as String
  }
  if (args.returnStatus) {
    returnStatus = args.returnStatus as boolean
  }
  if (args.returnStdout) {
    returnStdout = args.returnStdout as boolean
  }
  shell(script, sourceFile, encoding, returnStatus, returnStdout)
}

// overwrite file with text
@NonCPS
static String getStringInFile(String path) {
  def file = new File(path)
  file.exists() ? file.text : ''
}

@NonCPS
static void setStringInFile(String path, String value) {
  new File(path).newWriter().withWriter { w ->
    w << value
  }
}

@NonCPS
static HashMap parseJsonText(String jsonText) {
  return new HashMap<>(new JsonSlurper().parseText(jsonText) as LazyMap)
}

return this
