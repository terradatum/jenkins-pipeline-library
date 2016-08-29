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
  String jdkName = config.jdkName
  String mavenName = config.mavenName

  // We're wrapping this in a timeout - if it takes more than 180 minutes, kill it.
  timeout(time: 180, unit: 'MINUTES') {
    // See below for what this method does - we're passing an arbitrary environment
    // variable to it so that JAVA_OPTS and MAVEN_OPTS are set correctly.
    //noinspection GroovyAssignabilityCheck
    withMavenEnv {
      envVars = ["JAVA_OPTS=-Xmx1536m -Xms512m", "MAVEN_OPTS=-Xmx1536m -Xms512m"]
      jdkToolName = jdkName
      mavenToolName = mavenName
      // Actually run Maven!
      // The -Dmaven.repo.local=${pwd()}/.repository means that Maven will create a
      // .repository directory at the root of the build (which it gets from the
      // pwd() Workflow call) and use that for the local Maven repository.
      mvn = {
        wrap([$class: 'ConfigFileBuildWrapper', managedFiles: [[fileId: 'a451ec64-34b3-4ebc-9678-0198a2a130d5', replaceTokens: false, targetLocation: "${pwd()}/.m2/settings.xml", variable: '']]]) {
          sh "mvn -s ${pwd()}/.m2/settings.xml -V -U -B -Dmaven.repo.local=${pwd()}/.m2/repository ${args}"
        }
      }
    }
  }
}
