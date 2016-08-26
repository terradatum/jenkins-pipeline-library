#!/usr/bin/env groovy
package com.terradatum.jenkins.workflow

import com.cloudbees.groovy.cps.NonCPS
import jenkins.model.Jenkins

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
// persists current version in "${path to Jenkins full name}/currentVersion" file
// if a version is passed in, and is greater than the persisted version, then it overrides
// the persisted version and becomes the new version.
def Version incrementVersion(String project, VersionType versionType, Version version = null) {
  def path = "${getPathFromJenkinsFullName(project)}/currentVersion"
  def currentVersion = Version.valueOf('0.0.1')
  def nextVersion = currentVersion
  lock("${project}/currentVersion") {
    def versionString = getStringInFile(path)
    def persistedVersion = versionString ? Version.valueOf(versionString) : currentVersion
    if (version && version.compareWithBuildsTo(persistedVersion) < 0) {
      currentVersion = persistedVersion
    } else if (version) {
      currentVersion = version
    }
    switch (versionType) {
      case VersionType.Major:
        nextVersion = currentVersion.incrementMajorVersion()
        break
      case VersionType.Minor:
        nextVersion = currentVersion.incrementMinorVersion()
        break
      default:
        nextVersion = currentVersion.incrementPatchVersion()
        break
    }
    setStringInFile(path, nextVersion.toString())
  }
  nextVersion
}

// This method sets up the Maven and JDK tools, puts them in the environment along
// with whatever other arbitrary environment variables we passed in, and runs the
// body we passed in within that environment.
// TODO: wire this up for other project types.
def getProjectVersionString(ProjectType projectType) {
  def versionString = '0.0.1'
  switch (projectType) {
    case ProjectType.Maven:
      pom = readMavenPom file: 'pom.xml'
      versionString = pom.version
  }
  versionString
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