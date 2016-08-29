#!/usr/bin/env groovy
package com.terradatum.jenkins.workflow

import com.cloudbees.groovy.cps.NonCPS
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
 */

static def String getPathFromJenkinsFullName(String fullName) {
  Jenkins.instance.getItemByFullName(fullName).rootDir
}

static def removeTrailingSlash (String myString){
  if (myString.endsWith("/")) {
    return myString.substring(0, myString.length() - 1);
  }
  return myString
}

def getNexusLatestVersion(String artifact) {
  String latest = getNexusVersions(artifact).latest
  return Version.valueOf(latest)
}

def getNexusReleaseVersion(String artifact) {
  String release = getNexusVersions(artifact).release
  return Version.valueOf(release)
}

static def getNexusVersions(String artifact) {
  def repo = 'https://nexus.terradatum.com/content/groups/public/com/terradatum'
  artifact = removeTrailingSlash(artifact)

  def modelMetaData = new XmlSlurper().parse(repo+'/'+artifact+'/maven-metadata.xml')
  return modelMetaData.versioning
}

def getMaxNexusVersion(String project, String artifact, int major, int minor) {
  lock("${project}/maxNexusVersion") {
   def versioning = getNexusVersions(artifact)

    List<Version> versions = versioning.versions.find {
      def nexusVersion = Version.valueOf(it as String)
      if (nexusVersion.majorVersion == major && nexusVersion.minorVersion == minor) {
        nexusVersion
      }
    } as List<Version>
    return versions.max()
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
  }
  versionString
}

def getBuildMetadataVersion(Version version) {
  sh 'git rev-parse --short HEAD > commit'
  String commit = readFile('commit').trim()
  version.setBuildMetadata(commit)
}

def getTagVersion(Version version) {

  // if the repo has no tags this command will fail
  // if this command fails, and there is no config version, rethrow the error
  try {
    sh "git tag --sort version:refname | tail -1 > version.tmp"
  } catch(err) {
    echo "${err}"
    if (!version) {
      throw err
    }
  }

  String tag = readFile 'version.tmp'

  if (tag == null || tag.size() == 0){
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

def void gitMergeAndTag(String project, String targetBranch, String sourceBranch, Version releaseVersion) {
  sh 'git config user.email sysadmin@terradatum.com'
  sh 'git config user.name terradatum-automation'
  sh "git remote set-url origin git@github.com:${project}"

  sh "git checkout ${targetBranch}"
  sh "git merge origin/${sourceBranch}"

  sh 'git tag -d \$(git tag)'
  sh 'git fetch --tags'
  echo "New release version ${releaseVersion.normalVersion}"
  sh "git commit -a -m 'Release ${releaseVersion.normalVersion}'"

  sh "git tag -fa ${releaseVersion.normalVersion} -m 'Release version ${releaseVersion.normalVersion}'"
  sh "git push origin ${targetBranch}"
}

/*
 * NonCPS - non-sandboxed methods
 */
// read full text from file
@NonCPS
static def String getStringInFile(String path) {
  def file = new File(path)
  file.exists() ? file.text : ''
}

// overwrite file with text
@NonCPS
static def void setStringInFile(String path, String value) {
  new File(path).newWriter().withWriter { w ->
    w << value
  }
}

return this