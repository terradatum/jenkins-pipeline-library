#!/usr/bin/env groovy
package com.terradatum.jenkins.workflow

@GrabResolver(name='nexus', root='https://nexus.terradatum.com/content/groups/public/')
@Grab('com.github.zafarkhaja:java-semver:0.10-SNAPSHOT')

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
def Version incrementPatchVersion(String jenkinsFullName, Version version = null) {
  def path = "${getPathFromJenkinsFullName(jenkinsFullName)}/nextVersion"
  def nextVersion = Version.valueOf('0.0.1')
  lock("${jenkinsFullName}/nextVersion") {
    def persistedVersion = Version.valueOf(getStringInFile(path))
    if (version && persistedVersion && version.BUILD_AWARE_ORDER.lessThan(persistedVersion)) {
      nextVersion = persistedVersion
    } else if (version) {
      nextVersion = version
    } else if (persistedVersion) {
      nextVersion = persistedVersion
    }
    setStringInFile(path, nextVersion.incrementPatchVersion().toString())
  }
  nextVersion
}

// This method sets up the Maven and JDK tools, puts them in the environment along
// with whatever other arbitrary environment variables we passed in, and runs the
// body we passed in within that environment.
// TODO: wire this up for other project types.
def getProjectVersionString(ProjectType projectType) {
  def versionString
  switch (projectType) {
    case ProjectType.Maven:
      pom = readMavenPom file: 'pom.xml'
      versionString = pom.version
  }
  versionString
}

/*
 * Maven commands
 */

def String mvnArgs(Version version = null, args) {
  if (version) {
    if (version.buildMetadata) {
      return "-Drevision=\"${version.patchVersion}+${version.buildMetadata}\" ${args}"
    } else {
      return "-Drevision=\"${version.patchVersion}\" ${args}"
    }
  }
  return args
}

def mvn(String args) {
  // We're wrapping this in a timeout - if it takes more than 180 minutes, kill it.
  timeout(time: 180, unit: 'MINUTES') {
    // See below for what this method does - we're passing an arbitrary environment
    // variable to it so that JAVA_OPTS and MAVEN_OPTS are set correctly.
    withMavenEnv() {
      envVars = ["JAVA_OPTS=-Xmx1536m -Xms512m", "MAVEN_OPTS=-Xmx1536m -Xms512m"]
      maven = 'maven-3.3.9'
      jdk = "jdk-1.${env.JRE_MAJOR}.0_${env.JRE_UPDATE}"
      // Actually run Maven!
      // The -Dmaven.repo.local=${pwd()}/.repository means that Maven will create a
      // .repository directory at the root of the build (which it gets from the
      // pwd() Workflow call) and use that for the local Maven repository.
      mvn = wrap([$class: 'ConfigFileBuildWrapper', managedFiles: [[fileId: 'a451ec64-34b3-4ebc-9678-0198a2a130d5', replaceTokens: false, targetLocation: '', variable: 'MAVEN_SETTINGS_PATH']]]) {
        sh "mvn -s ${env.MAVEN_SETTINGS_PATH} -V -U -B -Dmaven.repo.local=${pwd()}/.repository ${args}"
      }
    }
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

return this