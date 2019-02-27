#!/usr/bin/env groovy

import com.terradatum.jenkins.workflow.Version
import hudson.model.Build

/**
 * Created by rbellamy on 2/26/19.
 */
def call(Build build) {
  Version lastSuccessfulBuildVersion = flow.getLastSuccessfulBuildVersion(build)

  echo "Last successful build version: ${lastSuccessfulBuildVersion}"

  return lastSuccessfulBuildVersion
}
