#!/usr/bin/env groovy
import com.terradatum.jenkins.workflow.*
/**
 * Created by rbellamy on 2/26/19.
 */
def call(build) {
  def flow = new TerradatumCommands()
  Version lastSuccessfulBuildVersion = flow.getLastSuccessfulBuildVersion(build)

  echo "Last successful build version: ${lastSuccessfulBuildVersion}"

  return lastSuccessfulBuildVersion
}
