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

  String sbtToolName = config.sbtToolName
  String jdkToolName = config.jdkToolName
  List envVars = config.envVars
  Closure sbt = config.sbt

  // Using the "tool" Workflow call automatically installs those tools on the
  // node.
  String sbtTool = tool name: sbtToolName, type: 'org.jvnet.hudson.plugins.SbtPluginBuilder$SbtInstallation'
  String jdkTool = tool name: jdkToolName, type: 'hudson.model.JDK'

  // Set JAVA_HOME, SBT_HOME and special PATH variables for the tools we're using.
  List sbtEnv = ["PATH+SBT=${sbtTool}/bin", "PATH+JDK=${jdkTool}/bin", "JAVA_HOME=${jdkTool}", "SBT_HOME=${sbtTool}"]

  // Add any additional environment variables.
  sbtEnv.addAll(envVars)

  // Invoke the body closure we're passed within the environment we've created.
  withEnv(sbtEnv) {
    sbt.call()
  }
}
