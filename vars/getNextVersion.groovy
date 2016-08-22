#!/usr/bin/env groovy
import com.terradatum.jenkins.workflow.TerradatumCommands
import com.terradatum.jenkins.workflow.Version
import com.terradatum.jenkins.workflow.VersionType
/**
 * Created by rbellamy on 8/19/16.
 */
def call(body) {
  // evaluate the body block, and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  String project = config.project
  VersionType versionType = config.versionType
  Version version = config.version

  def flow = new TerradatumCommands()
  flow.incrementVersion project, versionType, version
}
