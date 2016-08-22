#!/usr/bin/env groovy
import com.terradatum.jenkins.workflow.*
/**
 * Created by rbellamy on 8/19/16.
 *
 * Gets the version as defined in a project file, with the Patch version set to env.BUILD_NUMBER.
 *
 * TODO: currently only supports pom.xml, needs to support packages.json, build.sbt, etc.
 */
def call(body) {
  // evaluate the body block, and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  def flow = new TerradatumCommands()

  ProjectType projectType = config.projectType
  boolean fromTag = config.fromTag ?: false

  def projectVersion = flow.getProjectVersionString(projectType).tokenize('.')

  def major = projectVersion[0];
  def minor = projectVersion[1];
  def patch = env.BUILD_NUMBER

  Version workingVersion = Version.valueOf("${major}.${minor}.${patch}")

  if (fromTag) {
    //noinspection GroovyAssignabilityCheck
    workingVersion = getVersionFromTag {
      version = workingVersion
    }
  } else {
    sh 'git rev-parse --short HEAD > commit'
    def commit = readFile('commit').trim()

    workingVersion.buildMetadata = commit;
  }

  if (workingVersion) {
    echo "Working version: ${workingVersion}"
  } else {
    error 'Could not derive version'
  }
  return workingVersion
}
