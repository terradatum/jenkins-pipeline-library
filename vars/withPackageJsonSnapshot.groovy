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

  Version version = config.version
  Closure cmds = config.cmds

  flow.searchAndReplacePackageJsonSnapshot(version)
  cmds.call()
  flow.gitResetBranch()
}
