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
        }
        stage('Testing') {
            steps {
                sh 'mvn verify'
            }
            post {
                success {
                    junit 'target/surefire-reports/**/*.xml'
                }
            }
        }
        stage('SonarQube analysis') {
            steps {
                withSonarQubeEnv("SonarQube") {
                    sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.4.0.905:sonar'
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
        }
        stage('Publish JaCoCo Reports') {
            steps {
                script {
                    step([$class: 'JacocoPublisher', execPattern: '**/target/coverage-reports/*.exec'])
                }
            }
        }
        stage('Deploy Snapshots') {
            when {
                expression {
                    return params.branch == "origin/develop"
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
                    rtMaven.run pom: 'pom.xml', goals: 'install -U', buildInfo: buildInfo
                    publishBuildInfo server: server, buildInfo: buildInfo
                }
            }
        }
        stage('Release') {
            when {
                expression {
                    return params.branch == "origin/master"
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
        }
        stage('Promote to google cloud') {
            when {
                expression {
                    return params.promote == true
                }
            }
            steps {
                    dir('deployment'){
                        ansiblePlaybook([
            inventory   : 'hosts',
            playbook    : 'docker-setup.yml',
            installation: 'ansible',
        credentialsId: 'c0cc48f2-d914-46e1-b173-a979bce342e8',
            colorized   : true
          ])
                    }
                }
        }
    }
}

def static stripOrigin(String branch) {
    if (branch.startsWith("origin")) {
        return branch.substring(branch.indexOf('/') + 1, branch.length())
    }
    return branch
}

