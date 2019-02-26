#!/usr/bin/env groovy
import com.terradatum.jenkins.workflow.*
/**
 * Created by rbellamy on 2/26/19.
 */
def call(body) {
  // evaluate the body block, and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  def flow = new TerradatumCommands()

  def build = config.build

  Version lastSuccessfulBuildVersion = flow.getLastSuccessfulBuildVersion(build)

  echo "Last successful build version: ${lastSuccessfulBuildVersion}"

  return lastSuccessfulBuildVersion
}
