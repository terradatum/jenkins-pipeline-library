#!/usr/bin/env groovy
import com.terradatum.jenkins.workflow.TerradatumCommands
/**
 * @author rbellamy@terradatum.com 
 * @date 8/30/16
 */
def String call(Boolean returnStdout = false, String script) {
  def flow = new TerradatumCommands()

  if (returnStdout) {
    return flow.shell(returnStdout, script)
  }
  return flow.shell(script)
}
