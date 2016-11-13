#!/usr/bin/env groovy
import com.terradatum.jenkins.workflow.TerradatumCommands
/**
 * @author rbellamy@terradatum.com 
 * @date 8/30/16
 */
def String call(String script, String sourceFile = '', String encoding = 'UTF-8', boolean returnStatus = false, boolean returnStdout = false) {
  def flow = new TerradatumCommands()
  flow.shell(script, sourceFile, encoding, returnStatus, returnStdout)
}

def String call(Map args) {
  def flow = new TerradatumCommands()
  flow.shell(args)
}
