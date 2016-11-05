#!/usr/bin/env groovy
/**
 * Created by rbellamy on 8/19/16.
 */
def call(body) {
  // evaluate the body block, and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  String nodeToolName = config.nodeToolName
  List envVars = config.envVars
  // either npm or gulp or grunt or bower or yarn or...
  Closure cmd = config.cmd

  // Using the "tool" Workflow call automatically installs those tools on the
  // node.
  String nodeTool = tool name: nodeToolName, type: 'jenkins.plugins.nodejs.tools.NodeJSInstallation'

  // Set JAVA_HOME, MAVEN_HOME and special PATH variables for the tools we're using.
  List nodeEnv = ["PATH+NPM=${nodeTool}/bin", "NODE_HOME=${nodeTool}"]

  // Add any additional environment variables.
  if (envVars) {
    nodeEnv.addAll(envVars)
  }

  // Invoke the body closure we're passed within the environment we've created.
  withEnv(nodeEnv) {
    cmd.call()
  }
}
