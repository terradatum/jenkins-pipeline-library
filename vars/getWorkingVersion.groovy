#!/usr/bin/env groovy
import com.terradatum.jenkins.workflow.ProjectType
import com.terradatum.jenkins.workflow.TerradatumCommands
import com.terradatum.jenkins.workflow.Version

/**
 * Created by rbellamy on 8/19/16.
 */
def call(body) {
  // evaluate the body block, and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  def flow = new TerradatumCommands()
  def projectVersion = flow.getProjectVersionString(config.projectType as ProjectType).tokenize('.')

  def major = projectVersion[0];
  def minor = projectVersion[1];
  def patch = env.BUILD_NUMBER

  sh 'git rev-parse --short HEAD > commit'
  def commit = readFile('commit').trim()

  def workingVersion = Version.valueOf("${major}.${minor}.${patch}")
  workingVersion.buildMetadata = commit;
  if (fromTag) {
    //noinspection GroovyAssignabilityCheck
    workingVersion = getVersionFromTag {
      version = workingVersion
    }
  }

  if (workingVersion) {
    echo "Building version: ${workingVersion}"
  } else {
    error 'Could not derive BrokerMetrics version'
  }
  workingVersion
}
