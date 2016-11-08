#!/usr/bin/env groovy
import com.terradatum.jenkins.workflow.TerradatumCommands
/**
 * @author rbellamy@terradatum.com
 */
def call() {
  def flow = new TerradatumCommands()

  flow.dockerLogin()
}
