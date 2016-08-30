#!/usr/bin/env groovy
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

  String targetBranch = config.targetBranch
  Version releaseVersion = config.releaseVersion

  flow.gitTag(releaseVersion)
  flow.gitPush(targetBranch)
}
