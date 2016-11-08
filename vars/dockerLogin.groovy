#!/usr/bin/env groovy
import com.terradatum.jenkins.workflow.TerradatumCommands
/**
 * @author rbellamy@terradatum.com
 */
def call(void something) {
  def flow = new TerradatumCommands()

  flow.dockerLogin()
}
