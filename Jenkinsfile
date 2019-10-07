pipeline {
    agent { label "ads_sdk_worker" }

    stages {
        stage('Setup') {
            when {
                expression { env.BRANCH_NAME =~ /^PR-/ }
            }

            steps {
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
        }

        stage('Run tests') {
            when {
                expression { env.BRANCH_NAME =~ /^PR-/ }
            }

            parallel {
                stage ('hybrid_test') {
                    steps {
                        dir('results') {
                            script {
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

                                try {
                                    sharedLibs.removeFromGcp(artifactFolder)
                                } catch(e) {
                                    echo "Could not clean up artifacts from GCP: '$e'"
                                }
                            }
                        }
                    }
                }

                stage('system_test') {
                    steps {
                        dir('results') {
                            script {
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
                        }
                    }
                }
            }
        }
        stage('Post steps') {
            when {
                expression { env.BRANCH_NAME =~ /^PR-/ }
            }

            steps {
                script {
                    archiveArtifacts artifacts: "results/**", fingerprint: true
                    step ([$class: "JUnitResultArchiver", testResults: "results/**/*.xml"])
                    script {
                        slackChannel = "ads-sdk-notify"
                        sharedLibs.sendTestSummary(slackChannel)
                    }
                }
            }
        }
    }
}
