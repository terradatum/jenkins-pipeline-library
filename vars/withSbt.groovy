import com.terradatum.jenkins.workflow.Version

/**
 * @author rbellamy@terradatum.com 
 * @date 8/23/16
 *
 * This method sets up the Maven and JDK tools, along with whatever other arbitrary environment
 * variables we passed in, and runs the body we passed in within that environment.
 */
def call(body) {
  // evaluate the body block, and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  String args = config.args
  String jdkName = config.jdkName
  String sbtName = config.sbtName

  // We're wrapping this in a timeout - if it takes more than 180 minutes, kill it.
  timeout(time: 180, unit: 'MINUTES') {
    // See below for what this method does - we're passing an arbitrary environment
    // variable to it so that JAVA_OPTS and SBT_OPTS are set correctly.
    //noinspection GroovyAssignabilityCheck
    withSbtEnv {
      // the following line shouldn't be necessary, as the sbt-extras file should take care if
      // envVars = ["JAVA_OPTS=-Xmx1536m -Xms512m", "SBT_OPTS=-Xmx1536m -Xms512m"]
      jdkToolName = jdkName
      sbtToolName = sbtName
      sbtToolVersion = Version.valueOf(sbtName.tokenize("-")[1])
      // Actually run SBT!
      // The ivy cache will be placed one directory above the workspace... which should put it in the
      // github "repo branches" directory - this hopefully allows different branches to share the same
      // ivy cache.
      sbt = {
        // custom resolvers
        wrap([$class: 'ConfigFileBuildWrapper', managedFiles: [[fileId: 'a451ec64-34b3-4ebc-9678-0198a2a130d5', replaceTokens: false, targetLocation: "${pwd()}/../.sbt/repositories", variable: '']]]) {
          // credentials for pushing builds to Nexus
          wrap([$class: 'ConfigFileBuildWrapper', managedFiles: [[fileId: 'a451ec64-34b3-4ebc-9678-0198a2a130d5', replaceTokens: false, targetLocation: "${pwd()}/../.sbt/.credentials", variable: '']]]) {
            // global publishing script
            wrap([$class: 'ConfigFileBuildWrapper', managedFiles: [[fileId: 'a451ec64-34b3-4ebc-9678-0198a2a130d5', replaceTokens: false, targetLocation: "${pwd()}/../.sbt/${sbtToolVersion.majorVersion}.${sbtToolVersion.minorVersion}/global.sbt", variable: '']]]) {
              sh "sbt -batch -ivy ${pwd()}/../.ivy2 -sbt-dir ${pwd()}/../.sbt/${sbtToolVersion.majorVersion}.${sbtToolVersion.minorVersion} -Dsbt.repo=${pwd()}/../.sbt/repositories ${args}"
            }
          }
        }
      }
    }
  }
}
