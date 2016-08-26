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

  String mavenToolName = config.mavenToolName
  String jdkToolName = config.jdkToolName
  List envVars = config.envVars
  Closure mvn = config.mvn

  // Using the "tool" Workflow call automatically installs those tools on the
  // node.
  String mavenTool = tool name: mavenToolName, type: 'hudson.tasks.Maven$MavenInstallation'
  String jdkTool = tool name: jdkToolName, type: 'hudson.model.JDK'

  // Set JAVA_HOME, MAVEN_HOME and special PATH variables for the tools we're using.
  List mavenEnv = ["PATH+MVN=${mavenTool}/bin", "PATH+JDK=${jdkTool}/bin", "JAVA_HOME=${jdkTool}", "MAVEN_HOME=${mavenTool}"]

  // Add any additional environment variables.
  mavenEnv.addAll(envVars)

  // Invoke the body closure we're passed within the environment we've created.
  withEnv(mavenEnv) {
    mvn.call()
  }
}
