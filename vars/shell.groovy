#!/usr/bin/env groovy
import com.terradatum.jenkins.workflow.TerradatumCommands
/**
 * @author rbellamy@terradatum.com 
 * @date 8/30/16
 */
def String call(String script, String encoding = 'UTF-8', boolean returnStatus = false, boolean returnStdout = false) {
  def flow = new TerradatumCommands()

  flow.shell(script, encoding, returnStatus, returnStdout)
}

def String call(Map args) {
  def flow = new TerradatumCommands()

  String script = ''
  String encoding = 'UTF-8'
  boolean returnStatus = false
  boolean returnStdout = false
  if (args.script) {
    script = args.script as String
  }
  if (args.encoding) {
    encoding = args.encoding as String
  }
  if (args.returnStatus) {
    returnStatus = args.returnStatus as boolean
  }
  if (args.returnStdout) {
    returnStdout = args.returnStdout as boolean
  }
  flow.shell(script, encoding, returnStatus, returnStdout)
}
