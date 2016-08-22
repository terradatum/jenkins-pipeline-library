#!/usr/bin/env groovy
import com.terradatum.jenkins.workflow.Version

/**
 * Created by rbellamy on 8/19/16.
 */
def call(body) {
  // evaluate the body block, and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  String project = config.project
  String sourceBranch = config.sourceBranch
  String targetBranch = config.targetBranch
  Version releaseVersion = config.releaseVersion

  sh 'git config user.email sysadmin@terradatum.com'
  sh 'git config user.name terradatum-automation'
  sh "git remote set-url origin git@github.com:${project}"

  sh "git checkout ${targetBranch}"
  sh "git merge origin/${sourceBranch}"

  sh 'git tag -d \$(git tag)'
  sh 'git fetch --tags'
  echo "New release version ${releaseVersion.normalVersion}"
  sh "git commit -a -m 'Release ${releaseVersion.normalVersion}'"

  sh "git tag -fa ${releaseVersion.normalVersion} -m 'Release version ${releaseVersion.normalVersion}'"
  sh "git push origin ${targetBranch}"

}
