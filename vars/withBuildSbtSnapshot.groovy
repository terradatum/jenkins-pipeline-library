#!/usr/bin/env groovy
import com.terradatum.jenkins.workflow.TerradatumCommands
import com.terradatum.jenkins.workflow.Version

/**
 * @author rbellamy@terradatum.com 
 * @date 8/30/16
 */
def call(body) {
  // evaluate the body block, and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  def flow = new TerradatumCommands()

  String part = config.projectPart
  List<String> parts = config.projectParts
  Version version = config.version
  Closure cmds = config.cmds

  //noinspection GroovyAssignabilityCheck
  updateSbtDependencies {
    projectPart = part
    projectParts = parts
  }

  flow.updateBuildSbtSnapshotToVersion(version)
  cmds.call()
  flow.gitResetBranch()
}
