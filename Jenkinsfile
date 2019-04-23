pipeline {
    environment {
        registry = "dmoriarty/pbm"
        registryCredential = 'dockerhub'
    }
    agent any
    tools {
        maven 'maven'
        jdk 'Java 8'
    }
    stages {
        stage('Initialize') {
            steps {
                sh '''
                    echo "PATH = ${PATH}"
                    echo "M2_HOME = ${M2_HOME}"
                '''
                sh 'mvn --version'
                sh 'java -version'

                sh 'git checkout ' + stripOrigin("${params.branch}")
                sh 'git pull'
                sh 'git branch'

            }
        }
        stage('Build') {
            steps {
                sh 'mvn clean compile'
            }
            post {
                success {
                    slackSend color: 'good', message: 'maven build success'
                }
                failure {
                    slackSend color: 'bad', message: 'maven build failed'
                }
            }
        }
        stage('Testing') {
            steps {
                sh 'mvn verify'
            }
            post {
                success {
                    junit 'target/surefire-reports/**/*.xml'
                    slackSend color: 'good', message: 'Testing success'
                }
                failure {
                    slackSend color: 'bad', message: 'Testing failed'
                }
            }
        }
        stage('SonarQube analysis') {
            steps {
                withSonarQubeEnv("SonarQube") {
                    sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.4.0.905:sonar'
                }
            }
            post {
                success {
                    slackSend color: 'good', message: 'SonarQube analysis success'
                }
                failure {
                    slackSend color: 'bad', message: 'SonarQube analysis failed'
                }
            }
        }
        stage('SonarQube Gatekeeper') {
            steps {
                script {
                    sleep(5)
                    def qualityGate = waitForQualityGate()
                    if (qualityGate.status != "OK") {
                        error "Pipeline aborted due to quality gate coverage failure: ${qualityGate.status}"
                    }
                }
            }
            post {
                success {
                    slackSend color: 'good', message: 'SonarQube Gatekeeper success'
                }
                failure {
                    slackSend color: 'bad', message: 'SonarQube Gatekeeper failed'
                }
            }
        }
        stage('Publish JaCoCo Reports') {
            steps {
                script {
                    step([$class: 'JacocoPublisher', execPattern: '**/target/coverage-reports/*.exec'])
                }
            }
            post {
                success {
                    slackSend color: 'good', message: 'JaCoCo Reports published'
                }
                failure {
                    slackSend color: 'bad', message: 'JaCoCo Reports failed to publish'
                }
            }
        }
        stage('Deploy Snapshots') {
            when {
                expression {
                    return params.branch == "origin/artifact-working"
                }
            }
            steps {
                script {
                    def server = Artifactory.server "JFrog"
                    def buildInfo = Artifactory.newBuildInfo()
                    def rtMaven = Artifactory.newMavenBuild()
                    rtMaven.tool = 'maven'
                    rtMaven.deployer releaseRepo: 'libs-release-local', snapshotRepo: 'libs-snapshot-local', server: server
                    rtMaven.resolver releaseRepo: 'libs-release', snapshotRepo: 'libs-snapshot', server: server
                    rtMaven.run pom: 'pom.xml', goals: 'compile -U', buildInfo: buildInfo
                    publishBuildInfo server: server, buildInfo: buildInfo
                }
            }
            post {
                success {
                    slackSend color: 'good', message: 'Published jars to Artifactory'
                }
                failure {
                    slackSend color: 'bad', message: 'Failed to Publish jars to Artifactory'
                }
            }
        }
        stage('Release') {
            when {
                expression {
                    return params.branch == "origin/develop"
                }
            }
            steps {
                script {
                    dockerImage = docker.build registry + ":$BUILD_NUMBER"
                    docker.withRegistry( '', registryCredential ) {
                        dockerImage.push()
                    }
                    sh "docker rmi $registry:$BUILD_NUMBER"
                }
            }
            post {
                success {
                    slackSend color: 'good', message: 'Docker image built, tagged and pushed to docker hub'
                }
                failure {
                    slackSend color: 'bad', message: 'Release stage failed'
                }
            }
        }
        stage('Promote to google cloud') {
            when {
                expression {
                    return params.promote == true || params.branch == "origin/master"
                }
            }
            steps {
                    dir('deployment'){
                        ansiblePlaybook([
                            inventory   : 'hosts',
                            playbook    : 'create_vms_up_containers.yml',
                            installation: 'ansible',
                            hostKeyChecking: false,
                            colorized   : true
                        ])
                    }
            }
            post {
                success {
                    slackSend color: 'good', message: 'Promoted to environment'
                }
                failure {
                    slackSend color: 'bad', message: 'Failed to promote'
                }
            }
        }

    }
    post {
           // only triggered when blue or green sign
           success {
               slackSend color: 'good', message: 'build success'
           }
           // triggered when red sign
           failure {
               slackSend color: 'bad', message: 'build failed'
           }
           // trigger every-works
           always {
               slackSend color: 'always', message: "${currentBuild.result}"
           }
    }
}

def static stripOrigin(String branch) {
    if (branch.startsWith("origin")) {
        return branch.substring(branch.indexOf('/') + 1, branch.length())
    }
    return branch
}

