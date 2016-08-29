#!/usr/bin/env groovy
import com.terradatum.jenkins.workflow.TerradatumCommands
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

  def flow = new TerradatumCommands()

  Version version = config.version

  Version newVersion = flow.getTagVersion(version)

  echo "Tagged version: ${newVersion}"

  return newVersion
}
