#!/usr/bin/env groovy
import com.terradatum.jenkins.workflow.*
/**
 * Created by rbellamy on 8/19/16.
 */
def call(body) {
  // evaluate the body block, and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  def flow = new TerradatumCommands()

  String project = config.project
  Version version = config.version

  int patch = flow.setCurrentVersion(project, version)

  echo "Set currentVersion: ${version}"
}
