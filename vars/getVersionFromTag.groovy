#!/usr/bin/env groovy
import com.terradatum.jenkins.workflow.Version

/**
 * Created by rbellamy on 8/19/16.
 */
// if no previous tag found default 0.0.1 is used, else assume version is in the form major.minor or major.minor.patch version
// source here: https://github.com/fabric8io/jenkins-pipeline-library/blob/master/src/io/fabric8/Fabric8Commands.groovy
def call(body) {
  // evaluate the body block, and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  def version = config.version ?: Version.valueOf('0.0.1')

  // if the repo has no tags this command will fail
  sh "git tag --sort version:refname | tail -1 > version.tmp"

  String tag = readFile 'version.tmp'

  if (tag == null || tag.size() == 0){
    echo "no existing tag found using version ${version}"
    return version
  }

  tag = tag.trim()

  def semver = Version.valueOf(tag)
  def newVersion = version
  if (newVersion.compareWithBuildsTo(semver) < 0) {
    newVersion = semver
  }
  echo "New version is ${newVersion}"
  return newVersion
}
