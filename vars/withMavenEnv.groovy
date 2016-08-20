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

  // Using the "tool" Workflow call automatically installs those tools on the
  // node.
  String mvnTool = tool name: config.maven, type: 'hudson.tasks.Maven$MavenInstallation'
  String jdkTool = tool name: config.jdk, type: 'hudson.model.JDK'

  // Set JAVA_HOME, MAVEN_HOME and special PATH variables for the tools we're using.
  List mvnEnv = ["PATH+MVN=${mvnTool}/bin", "PATH+JDK=${jdkTool}/bin", "JAVA_HOME=${jdkTool}", "MAVEN_HOME=${mvnTool}"]

  // Add any additional environment variables.
  mvnEnv.addAll(config.envVars as List)

  // Invoke the body closure we're passed within the environment we've created.
  withEnv(mvnEnv) {
    config.mvn.call()
  }
}
