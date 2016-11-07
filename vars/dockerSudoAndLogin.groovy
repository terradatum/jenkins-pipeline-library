#!/usr/bin/env groovy
import com.terradatum.jenkins.workflow.TerradatumCommands
/**
 * @author rbellamy@terradatum.com 
 * @date 8/30/16
 */
def call() {
  def flow = new TerradatumCommands()

  flow.dockerSudoAndLogin()
}
