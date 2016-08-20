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
  sh 'git config user.email sysadmin@terradatum.com'
  sh 'git config user.name terradatum-automation'
  sh "git remote set-url origin git@github.com:${config.project}"

  sh "git checkout ${config.targetBranch}"
  sh "git merge origin/${config.sourceBranch}"

  sh 'git tag -d \$(git tag)'
  sh 'git fetch --tags'
  echo "New release version ${config.releaseVersion.normalVersion}"
  sh "git commit -a -m 'Release ${config.releaseVersion.normalVersion}'"

  sh "git tag -fa ${config.releaseVersion.normalVersion} -m 'Release version ${config.releaseVersion.normalVersion}'"
  sh "git push origin ${config.targetBranch}"

}
