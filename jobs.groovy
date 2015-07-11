def project = 'Azulinho/flocker'
def git_url = "https://github.com/${project}.git"
def dashProject = "${project}".replace('/','-')
def branchApi = new URL("https://api.github.com/repos/${project}/branches")
def branches = new groovy.json.JsonSlurper().parse(branchApi.newReader())



def aws_ubuntu_trusty(project, git_url, branch) {

  folder("${project}/${branch}") {
    displayName("${branch}")
  }

  job ("${project}/${branch}/aws_ubuntu_trusty") {
      scm {
          git("${git_url}", "${branch}")
      }
      steps {
          shell("export PATH=/usr/local/bin:$PATH ; virtualenv -p python2.7 --clear flocker-admin/venv")
          shell("flocker-admin/venv/bin/pip install .")
          shell("flocker-admin/venv/bin/pip install  .")
          shell("flocker-admin/venv/bin/pip install Flocker[doc,dev,release]")
          shell("flocker-admin/venv/bin/py.test --junitxml results.xml flocker")
      }
      publishers {
          archiveJunit('results.xml') {
               retainLongStdout(true)
               testDataPublishers {
                    allowClaimingOfFailedTests()
                    publishTestAttachments()
                    publishTestStabilityData()
                    publishFlakyTestsReport()
                }
          }
      }
  }
}


folder("${dashProject}") {
    displayName("${dashProject}")
}

branches << ['name':'master']
branches.each {

  branchName = "${it.name}"
  dashBranchName = "${branchName}".replace("/","-")


  folder("${dashProject}/${branchName}") {
    displayName("${branchName}")
  }

  aws_ubuntu_trusty("${dashProject}", "${git_url}","${branchName}")

  _flow = """
      parallel (
        { build("PROJECT/BRANCH/aws_ubuntu_trusty") }
      )
      """.replace('PROJECT', "${dashProject}").replace('BRANCH', "${branchName}")

  buildFlowJob("${dashProject}/${branchName}/_main") {
      buildFlow("${_flow}")
      publishers {
          aggregateBuildFlowTests()
      }
  }
}
