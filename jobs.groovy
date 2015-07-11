def project = 'Azulinho/flocker'
def git_url = "https://github.com/${project}.git"
def dashProject = "${project}".replace('/','-')
def branchApi = new URL("https://api.github.com/repos/${project}/branches")
def branches = new groovy.json.JsonSlurper().parse(branchApi.newReader())

def aws_ubuntu_trusty(project, git_url, branch) {
  folder("${project}/${branch}") {
    displayName("${branch}")
  }

  job ("${project}/${branch}/aws_ubuntu_trusty_acceptance") {
      scm {
          git("${git_url}", "${branch}")
      }
      steps {
          shell("export PATH=/usr/local/bin:$PATH ; virtualenv -p python2.7 --clear flocker-admin/venv")
          shell("flocker-admin/venv/bin/pip install .")
          shell("flocker-admin/venv/bin/pip install  .")
          shell("flocker-admin/venv/bin/pip install Flocker[doc,dev,release]")
          shell("flocker-admin/venv/bin/py.test --junitxml results.xml flocker/acceptance")
      }
      publishers {
          archiveArtifacts('results.xml')
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
  job ("${project}/${branch}/aws_ubuntu_trusty_cli") {
      scm {
          git("${git_url}", "${branch}")
      }
      steps {
          shell("export PATH=/usr/local/bin:$PATH ; virtualenv -p python2.7 --clear flocker-admin/venv")
          shell("flocker-admin/venv/bin/pip install .")
          shell("flocker-admin/venv/bin/pip install  .")
          shell("flocker-admin/venv/bin/pip install Flocker[doc,dev,release]")
          shell("flocker-admin/venv/bin/py.test --junitxml results.xml flocker/cli")
      }
      publishers {
          archiveArtifacts('results.xml')
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
  job ("${project}/${branch}/aws_ubuntu_trusty_volume") {
      scm {
          git("${git_url}", "${branch}")
      }
      steps {
          shell("export PATH=/usr/local/bin:$PATH ; virtualenv -p python2.7 --clear flocker-admin/venv")
          shell("flocker-admin/venv/bin/pip install .")
          shell("flocker-admin/venv/bin/pip install  .")
          shell("flocker-admin/venv/bin/pip install Flocker[doc,dev,release]")
          shell("flocker-admin/venv/bin/py.test --junitxml results.xml flocker/volume")
      }
      publishers {
          archiveArtifacts('results.xml')
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

  multiJob {
      steps {
          phase('parallel_tests', 'ALWAYS') {
              phaseName 'parallel_tests'
              job("${${dashProject}/${branchName}/aws_ubuntu_trusty_acceptance")
              job("${${dashProject}/${branchName}/aws_ubuntu_trusty_cli")
              job("${${dashProject}/${branchName}/aws_ubuntu_trusty_volume")
          }
          copyArtifacts('Azulinho-flocker/master/aws_ubuntu_trusty_acceptance) {
              includePatterns('results.xml)
              targetDirectory('aws_ubuntu_trusty_acceptance')
              fingerprintArtifacts(true)
              buildSelector {
                  workspace()
              }
          }
          copyArtifacts('Azulinho-flocker/master/aws_ubuntu_cli) {
              includePatterns('results.xml)
              targetDirectory('aws_ubuntu_trusty_cli')
              fingerprintArtifacts(true)
              buildSelector {
                  workspace()
              }
          }
          copyArtifacts('Azulinho-flocker/master/aws_ubuntu_trusty_volume) {
              includePatterns('results.xml)
              targetDirectory('aws_ubuntu_trusty_volume')
              fingerprintArtifacts(true)
              buildSelector {
                  workspace()
              }
          }

      }
      publishers {
          archiveJunit('**/results.xml') {
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
