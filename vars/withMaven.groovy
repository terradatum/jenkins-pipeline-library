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
  String mavenName = config.mavenName
  List environment = config.environment

  // We're wrapping this in a timeout - if it takes more than 180 minutes, kill it.
  timeout(time: 180, unit: 'MINUTES') {
    // See below for what this method does - we're passing an arbitrary environment
    // variable to it so that JAVA_OPTS and MAVEN_OPTS are set correctly.
    //noinspection GroovyAssignabilityCheck
    withMavenEnv {
      envVars = ["JAVA_OPTS=-Xmx1536m -Xms512m", "MAVEN_OPTS=-Xmx1536m -Xms512m"]
      jdkToolName = jdkName
      mavenToolName = mavenName
      envVars = environment
      // Actually run Maven!
      // The -Dmaven.repo.local=${pwd()}/.repository means that Maven will create a
      // .repository directory at the in the "git repository" root of the build (which it gets from the
      // pwd() Workflow call) and use that for the local Maven repository.
      // e.g. using "${pwd()}/../.m2" places the repository in a global location, /var/lib/jenkins/workspace, while
      //      using "${pwd()}/.m2" places the repository in the project location, /var/lib/jenkins/workspace/<project directory>
      mvn = {
        configFileProvider([configFile(fileId: 'a451ec64-34b3-4ebc-9678-0198a2a130d5', targetLocation: "${pwd()}/../.m2/settings.xml")]) {
          sh "mvn -s ${pwd()}/../.m2/settings.xml -V -U -B -Dmaven.repo.local=${pwd()}/../.m2/repository ${args}"
        }
      }
    }
  }
}
