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

  List envVars = config.envVars
  Closure deis = config.deis

  // Using the "tool" Workflow call automatically installs those tools on the
  // node.
  String kubectlTool = tool name: 'kubectl', type: 'com.cloudbees.jenkins.plugins.customtools.CustomTool'
  String deisTool = tool name: 'deis', type: 'com.cloudbees.jenkins.plugins.customtools.CustomTool'

  // Set JAVA_HOME, MAVEN_HOME and special PATH variables for the tools we're using.
  List kubectlEnv = ["PATH+KUBECTL=${kubectlTool}/bin", "PATH+DEIS=${deisTool}/bin", "KUBECONFIG=${pwd()}/../.kube/config"]

  // Add any additional environment variables.
  if (envVars) {
    kubectlEnv.addAll(envVars)
  }
  kubectlEnv.addAll(env)

  // Invoke the body closure we're passed within the environment we've created.
  withEnv(kubectlEnv) {
    deis.call()
  }
}
