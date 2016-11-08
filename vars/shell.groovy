#!/usr/bin/env groovy
import com.terradatum.jenkins.workflow.TerradatumCommands
/**
 * @author rbellamy@terradatum.com 
 * @date 8/30/16
 */
def String call(Map args) {
  def flow = new TerradatumCommands()

  if (args.returnStdout) {
    return flow.shell(args.returnStdout as Boolean, args.script as String)
  }
  return flow.shell(args.script as String)
}
