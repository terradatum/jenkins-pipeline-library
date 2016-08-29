#!/usr/bin/env groovy
import com.terradatum.jenkins.workflow.*
/**
 * Created by rbellamy on 8/19/16.
 *
 * Gets the version as defined in a project file as Major.Minor.Patch
 * If the Patch version is not a number, it is set to 0.
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

  def projectVersionString = flow.getProjectVersionString(projectType).tokenize('.')

  def major = projectVersionString[0];
  def minor = projectVersionString[1];
  def patch
  if (projectVersionString[2]?.trim() && projectVersionString[2].isNumber()) {
    patch = projectVersionString[2]
  } else {
    patch = 0
  }

  Version projectVersion = Version.valueOf("${major}.${minor}.${patch}")

  echo "Project version: ${projectVersion}"

  return projectVersion
}
