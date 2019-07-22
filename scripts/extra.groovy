def main() {
    // commitId value is not used, but is here for learning purposes
    commitId = sh(returnStdout: true, script: 'git rev-parse HEAD').trim()
    echo "#### Commit $commitId ####"

    if (env.BRANCH_NAME =~ /^PR-/) {
        stage('checkout') {
            dir('sharedLibs') {
                checkout(
                    [$class: 'GitSCM', branches: [[name: 'master']],
                    userRemoteConfigs: [[credentialsId: 'applifier-readonly-jenkins-bot',
                    url: 'https://github.com/Applifier/unity-ads-sdk-tests.git']]]
                )
                script {
                    sharedLibs = load 'sharedLibs.groovy'
                }
            }
        }

        stage('Run tests') {
            dir('results') {
                parallel (
                    'hybrid_test': {
                        def jobName = "ads-sdk-hybrid-test-android"
                        def build_ = build(
                          job: "Applifier/unity-ads-sdk-tests/$jobName",
                          propagate: false,
                          wait: true,
                          parameters: [
                            string(name: 'UNITY_ADS_ANDROID_BRANCH', value: env.CHANGE_BRANCH),
                          ]
                        )

                        def artifactFolder = "$jobName/$build_.number"
                        dir(jobName) {
                            sharedLibs.downloadFromGcp("$artifactFolder/*")
                        }
                        sharedLibs.removeFromGcp(artifactFolder)
                    },

                    'system_test': {
                        def jobName = "ads-sdk-systest-android"
                        build(
                          job: "Applifier/unity-ads-sdk-tests/$jobName",
                          propagate: false,
                          wait: false,
                          parameters: [
                            string(name: 'UNITY_ADS_ANDROID_BRANCH', value: env.CHANGE_BRANCH)
                          ],
                        )
                    }
                )
            }
            archiveArtifacts artifacts: "results/**", fingerprint: true
            step ([$class: "JUnitResultArchiver", testResults: "results/**/*.xml"])

            slackChannel = "ads-sdk-notify"
            sharedLibs.sendTestSummary(slackChannel)
        }
    }
}

return this;
