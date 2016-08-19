package com.terradatum.jenkins.workflow

@Grab('com.github.zafarkhaja:java-semver:0.9.0')

import com.cloudbees.groovy.cps.NonCPS
import com.github.zafarkhaja.semver.Version
import jenkins.model.Jenkins

/**
 * Created by rbellamy on 8/15/16.
 *
 * When working on this script using IntelliJ, you'll want to run the following
 * commands from the command line:
 * 1. mvn dependency:get -Dartifact=com.cloudbees:groovy-cps:1.9
 * 2. mvn dependency:get -Dartifact=com.github.zafarkhaja:java-semver:0.9.0
 * 3. mvn dependency:get -Dartifact=org.6wind.jenkins:lockable-resources:1.10
 * 4. mvn dependency:get -Dartifact=org.jenkins-ci.plugins.workflow:workflow-api:2.1
 */

/*
 * File and path operations, blocking and non-blocking
 */

static def String getPathFromJenkinsFullName(String fullName) {
  Jenkins.instance.getItemByFullName(fullName).rootDir
}

/*
 * version processing
 */

// blocking call to get version, increment, and return
def Version incrementPatchVersion(String jenkinsFullName) {
  def version = Version.valueOf('0.0.1')
  def path = "${getPathFromJenkinsFullName(jenkinsFullName)}/nextVersion"
  lock(path) {
    version = Version.valueOf(getStringInFile(path))
    setStringInFile(path, version.incrementPatchVersion().toString())
  }
  version
}

// This method sets up the Maven and JDK tools, puts them in the environment along
// with whatever other arbitrary environment variables we passed in, and runs the
// body we passed in within that environment.
def getProjectVersionString() {
  pom = readMavenPom file: 'pom.xml'
  pom.version
}

def Version getWorkingVersion(boolean fromTag) {
  def projectVersion = getProjectVersionString().tokenize('.')
  int major = projectVersion[0];
  int minor = projectVersion[1];

  int patch = env.BUILD_NUMBER

  sh 'git rev-parse --short HEAD > commit'
  def commit = readFile('commit').trim()

  def workingVersion = Version.valueOf("${major}.${minor}.${patch}")
  workingVersion.buildMetadata = commit;
  if (fromTag) {
    workingVersion = getNewVersionFromTag(workingVersion)
  }
  if (workingVersion) {
    echo "Building version: ${workingVersion}"
  } else {
    error 'Could not derive BrokerMetrics version'
  }
  workingVersion
}

// if no previous tag found default 0.0.1 is used, else assume version is in the form major.minor or major.minor.patch version
// source here: https://github.com/fabric8io/jenkins-pipeline-library/blob/master/src/io/fabric8/Fabric8Commands.groovy
def Version getNewVersionFromTag(Version workingVersion = null){

  def version = Version.valueOf('0.0.1')

  // if the repo has no tags this command will fail
  sh "git tag --sort version:refname | tail -1 > version.tmp"

  String tag = readFile 'version.tmp'

  if (tag == null || tag.size() == 0){
    echo "no existing tag found using version ${version}"
    return version
  }

  tag = tag.trim()

  def semver = Version.valueOf(tag)

  def newVersion
  if (workingVersion.greaterThan(semver)) {
    newVersion = workingVersion
  } else {
    newVersion = semver
  }
  echo "New version is ${newVersion}"
  return newVersion
}

/*
 * Tag management
 */
def void tagAndMergeWithMaster(Version workingVersion, String remoteRepoPath) {
  stage 'tag and merge with master'
  sh 'git config user.email sysadmin@terradatum.com'
  sh 'git config user.name terradatum-automation'
  sh "git remote set-url origin git@github.com:${remoteRepoPath}"

  sh 'git checkout master'
  sh "git merge origin/${env.BRANCH_NAME}"

  sh 'git tag -d \$(git tag)'
  sh 'git fetch --tags'
  echo "New release version ${workingVersion.normalVersion}"
  sh "git commit -a -m 'Release ${workingVersion.normalVersion}'"
  pushTag(workingVersion)
}

def pushTag(Version releaseVersion){
  sh "git tag -fa ${releaseVersion.normalVersion} -m 'Release version ${releaseVersion.normalVersion}'"
  sh "git push origin master"
}

/*
 * Maven commands
 */

def String mvnArgs(Version workingVersion = null, args) {
  if (workingVersion) {
    if (workingVersion.buildMetadata) {
      return "-Drevision=\"${workingVersion.patchVersion}+${workingVersion.buildMetadata}\" ${args}"
    } else {
      return "-Drevision=\"${workingVersion.patchVersion}\" ${args}"
    }
  }
  return args
}

def mvn(String args) {
  // We're wrapping this in a timeout - if it takes more than 180 minutes, kill it.
  timeout(time: 180, unit: 'MINUTES') {
    // See below for what this method does - we're passing an arbitrary environment
    // variable to it so that JAVA_OPTS and MAVEN_OPTS are set correctly.
    withMavenEnv(["JAVA_OPTS=-Xmx1536m -Xms512m",
                  "MAVEN_OPTS=-Xmx1536m -Xms512m"]) {
      // Actually run Maven!
      // The -Dmaven.repo.local=${pwd()}/.repository means that Maven will create a
      // .repository directory at the root of the build (which it gets from the
      // pwd() Workflow call) and use that for the local Maven repository.
      wrap([$class: 'ConfigFileBuildWrapper', managedFiles: [[fileId: 'a451ec64-34b3-4ebc-9678-0198a2a130d5', replaceTokens: false, targetLocation: '', variable: 'MAVEN_SETTINGS_PATH']]]) {
        sh "mvn -s ${env.MAVEN_SETTINGS_PATH} -V -U -B -Dmaven.repo.local=${pwd()}/.repository ${args}"
      }
    }
  }
}

void withMavenEnv(List envVars = [], def body) {
  // The names here are currently hardcoded for my test environment. This needs
  // to be made more flexible.
  // Using the "tool" Workflow call automatically installs those tools on the
  // node.
  String mvnTool = tool name: 'maven-3.3.9', type: 'hudson.tasks.Maven$MavenInstallation'
  String jdkTool = tool name: "jdk-1.${env.JRE_MAJOR}.0_${env.JRE_UPDATE}", type: 'hudson.model.JDK'

  // Set JAVA_HOME, MAVEN_HOME and special PATH variables for the tools we're using.
  List mvnEnv = ["PATH+MVN=${mvnTool}/bin", "PATH+JDK=${jdkTool}/bin", "JAVA_HOME=${jdkTool}", "MAVEN_HOME=${mvnTool}"]

  // Add any additional environment variables.
  mvnEnv.addAll(envVars)

  // Invoke the body closure we're passed within the environment we've created.
  withEnv(mvnEnv) {
    body.call()
  }
}

/*
 * NonCPS - non-sandboxed methods
 */
// read full text from file
@NonCPS
def String getStringInFile(String path) {
  new File(path).text
}

// overwrite file with text
@NonCPS
def void setStringInFile(String path, String value) {
  new File(path).newWriter().withWriter { w ->
    w << value
  }
}
