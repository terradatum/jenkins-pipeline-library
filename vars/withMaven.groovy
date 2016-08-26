/**
 * @author rbellamy@terradatum.com 
 * @date 8/23/16
 */
def call(body) {
  // evaluate the body block, and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  String args = config.args
  String mavenName = config.mavenName
  String jdkToolName = config.jdkName

  // We're wrapping this in a timeout - if it takes more than 180 minutes, kill it.
  timeout(time: 180, unit: 'MINUTES') {
    // See below for what this method does - we're passing an arbitrary environment
    // variable to it so that JAVA_OPTS and MAVEN_OPTS are set correctly.
    //noinspection GroovyAssignabilityCheck
    withMavenEnv {
      envVars = ["JAVA_OPTS=-Xmx1536m -Xms512m", "MAVEN_OPTS=-Xmx1536m -Xms512m"]
      mavenToolName = mavenName
      jdkToolName = jdkName
      // Actually run Maven!
      // The -Dmaven.repo.local=${pwd()}/.repository means that Maven will create a
      // .repository directory at the root of the build (which it gets from the
      // pwd() Workflow call) and use that for the local Maven repository.
      mvn = {
        wrap([$class: 'ConfigFileBuildWrapper', managedFiles: [[fileId: 'a451ec64-34b3-4ebc-9678-0198a2a130d5', replaceTokens: false, targetLocation: '', variable: 'mavenSettingsPath']]]) {
          sh "mvn -s ${mavenSettingsPath} -V -U -B -Dmaven.repo.local=${pwd()}/.repository ${args}"
        }
      }
    }
  }
}
