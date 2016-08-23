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

  Version version = config.version

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

  echo "Tagged version: ${newVersion}"

  return newVersion
}
