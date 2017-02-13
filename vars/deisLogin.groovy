#!/usr/bin/env groovy
import com.terradatum.jenkins.workflow.TerradatumCommands
/**
 * @author rbellamy@terradatum.com
 */
def call(body) {
  // evaluate the body block, and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  String controller = config.controller

  def flow = new TerradatumCommands()

  withCredentials([usernamePassword(credentialsId: '1bad893d-fdd6-4ddf-b7bb-5ebbffa195b6', passwordVariable: 'PASSWORD', usernameVariable: 'USERNAME')]) {
    withDeis {
      args = "login ${controller} --username=${env.USERNAME} --password=${env.PASSWORD}"
    }
  }
}
