/**
 * @author rbellamy@terradatum.com
 * @date 11/4/16
 *
 * This method sets up the Node tools, along with whatever other arbitrary environment
 * variables we passed in, and runs the body we passed in within that environment.
 */
def call(body) {
  // evaluate the body block, and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  String args = config.args
  String nodeName = config.nodeName
  List environment = config.environment

  // We're wrapping this in a timeout - if it takes more than 180 minutes, kill it.
  timeout(time: 180, unit: 'MINUTES') {
    //noinspection GroovyAssignabilityCheck
    withNodeEnv {
      nodeToolName = nodeName
      envVars = environment
      // Actually run node!
      cmd = {
        wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'XTerm']) {
          shell "node ${args}"
        }
      }
    }
  }
}
