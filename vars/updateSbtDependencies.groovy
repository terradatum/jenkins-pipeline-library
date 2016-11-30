#!/usr/bin/env groovy
import com.terradatum.jenkins.workflow.TerradatumCommands
/**
 * @author rbellamy@terradatum.com
 */
def call() {
  // evaluate the body block, and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  String projectPart = config.projectPart
  def flow = new TerradatumCommands()

  flow.updateSbtDependencies(projectPart)
}
