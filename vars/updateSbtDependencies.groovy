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
  List<String> projectParts = config.projectParts

  def flow = new TerradatumCommands()

  if (projectPart) {
    if (!projectParts) {
      projectParts = new ArrayList<>()
    }
    projectParts.add(projectPart)
  }

  if (projectParts) {
    projectParts.each { p ->
      flow.updateSbtDependencies(p)
    }
  }
}
