import com.terradatum.jenkins.workflow.TerradatumCommands

/**
 * @author rbellamy@terradatum.com
 * @date 2/13/17
 */
def call(body) {
  // evaluate the body block, and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  def flow = new TerradatumCommands()

  String controller = config.controller
  String username = config.username
  String password = config.password
  String app = config.app

  String args = config.args
  List deisEnv = config.deisEnv

  timeout(time: 180, unit: 'MINUTES') {
    //noinspection GroovyAssignabilityCheck
    withKubectlEnv {
      envVars = deisEnv
      deis = {
        configFileProvider([
            configFile(fileId: '7dcedacc-d280-4d68-b98f-d6a20fd40c67', targetLocation: "${pwd()}/../.kube/config"),
            configFile(fileId: '5db9144c-b344-416d-8a65-daeaf96662d7', targetLocation: "${pwd()}/../.kube/credentials/ca-key.pem"),
            configFile(fileId: '2fdcd2eb-e60f-48da-8c6a-14573aeac69b', targetLocation: "${pwd()}/../.kube/credentials/ca.pem"),
            configFile(fileId: 'eacf7b0f-49dc-43df-ab6a-751ec54b3a1c', targetLocation: "${pwd()}/../.kube/credentials/admin-key.pem"),
            configFile(fileId: 'ee9dce01-cd6c-4807-98df-d47d2b0dff6f', targetLocation: "${pwd()}/../.kube/credentials/admin.pem")
        ]) {
          wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'XTerm']) {
            flow.deisLogin(controller, username, password)
            flow.deisAppRegistry(app)
            shell "deis ${args}"
          }
        }
      }
    }
  }
}
