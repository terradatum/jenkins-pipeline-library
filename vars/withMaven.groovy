import com.terradatum.jenkins.workflow.TerradatumCommands

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

  def flow = new TerradatumCommands()

  flow.maven(args)
}
