#!/usr/bin/env groovy
import com.terradatum.jenkins.workflow.TerradatumCommands
import com.terradatum.jenkins.workflow.Version
/**
 * Created by rbellamy on 8/19/16.
 *
 * Gets the next version number, whether Major, Minor or Patch. Persists the version at the project.
 */
def call(body) {
  // evaluate the body block, and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  def flow = new TerradatumCommands()

  String project = config.project
  String artifact = config.artifact
  Version version = config.version

  Version maxNexusVersion = flow.getMaxNexusVersion(project, artifact, version.majorVersion, version.minorVersion) as Version

  echo "Max Nexus version: ${maxNexusVersion}"

  return maxNexusVersion
}
