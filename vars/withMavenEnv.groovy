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

  String maven = config.maven
  String jdk = config.jdk
  List envVars = config.envVars
  Closure mvn = config.mvn

  // Using the "tool" Workflow call automatically installs those tools on the
  // node.
  String mvnTool = tool name: maven, type: 'hudson.tasks.Maven$MavenInstallation'
  String jdkTool = tool name: jdk, type: 'hudson.model.JDK'

  // Set JAVA_HOME, MAVEN_HOME and special PATH variables for the tools we're using.
  List mvnEnv = ["PATH+MVN=${mvnTool}/bin", "PATH+JDK=${jdkTool}/bin", "JAVA_HOME=${jdkTool}", "MAVEN_HOME=${mvnTool}"]

  // Add any additional environment variables.
  mvnEnv.addAll(envVars)

  // Invoke the body closure we're passed within the environment we've created.
  withEnv(mvnEnv) {
    mvn.call()
  }
}
