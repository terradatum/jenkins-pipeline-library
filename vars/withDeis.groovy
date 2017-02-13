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

  String args = config.args
  List deisEnv = config.deisEnv

  timeout(time: 180, unit: 'MINUTES') {
    //noinspection GroovyAssignabilityCheck
    withKubectlEnv {
      envVars = deisEnv
      deis = {
        configFileProvider([configFile(fileId: '7dcedacc-d280-4d68-b98f-d6a20fd40c67', targetLocation: "${pwd()}/../.kube/config")]) {
          withCredentials([
              file(credentialsId: '73cb1a83-be57-4878-a894-1a01047be784', variable: 'CA_KEY_PEM'),
              file(credentialsId: 'ed53a1d3-2221-493e-b01b-69a1efa01175', variable: 'CA_KEY_PEM_ENC'),
              file(credentialsId: '55f75a92-9b93-45c2-833a-330a2da323be', variable: 'CA_PEM'),
              file(credentialsId: '72c320e8-0e42-4369-810f-f1993862d5fb', variable: 'CA_PEM_ENC'),
              file(credentialsId: 'c79e802e-831b-415f-b7e1-7c1d5b70278a', variable: 'ADMIN_KEY_PEM'),
              file(credentialsId: '345aa25e-3011-4293-addf-5a0643915d0f', variable: 'ADMIN_KEY_PEM_ENC'),
              file(credentialsId: '93fcf5e7-b207-4511-b027-f4ac1223a8f0', variable: 'ADMIN_PEM'),
              file(credentialsId: '773d82c1-62e6-40ba-ba1d-2deedc561db5', variable: 'ADMIN_PEM_ENC')
          ]) {
            shell "cp -frv ${env.CA_KEY_PEM} ${pwd()}/../.kube/credentals/"
            shell "cp -frv ${env.CA_KEY_PEM_ENC} ${pwd()}/../.kube/credentals/"
            shell "cp -frv ${env.CA_PEM} ${pwd()}/../.kube/credentals/"
            shell "cp -frv ${env.CA_PEM_ENC} ${pwd()}/../.kube/credentals/"
            shell "cp -frv ${env.ADMIN_KEY_PEM} ${pwd()}/../.kube/credentals/"
            shell "cp -frv ${env.ADMIN_KEY_PEM_ENC} ${pwd()}/../.kube/credentals/"
            shell "cp -frv ${env.ADMIN_PEM} ${pwd()}/../.kube/credentals/"
            shell "cp -frv ${env.ADMIN_PEM_ENC} ${pwd()}/../.kube/credentals/"

            wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'XTerm']) {
              shell "deis ${args}"
            }
          }
        }
      }
    }
  }
}
