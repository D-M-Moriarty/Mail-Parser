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
        stage('Promote') {
            when {
                expression {
                    return params.promote == true
                }
            }
            steps {
                script {
                    sshagent(credentials: ['269031bb-f19f-45f0-b6b8-c878e67a087d']) {

                        sh 'git checkout ' + stripOrigin("${params.branch}")
                        sh 'git fetch --all'

                        TAGS = sh(script: "git tag -l --sort=-creatordate | head -n10", returnStdout: true).trim()

                        CHOSEN_RELEASE = input id: 'Tag',
                                message: 'Choose a version to promote',
                                parameters: [
                                        choice(choices: "${TAGS}",
                                                description: "Please choose one.",
                                                name: "CHOOSE_RELEASE")
                                ]//,
                        //submitter: 'root, dmoriarty', submitterParameter: 'tag'

                        sh "echo ${CHOSEN_RELEASE}"
                        // set file for reboot
                        sh '#!/bin/bash\n' +
                                'ssh ec2-user@pbm << _EOF_\n' +
                                "  sudo echo -e '#!/bin/bash\n" +
                                "  release_version=${CHOSEN_RELEASE}\n" +
                                "  sudo systemctl start docker.service\n" +
                                "  cd /home/ec2-user\n" +
                                '  docker-compose stop\n' +
                                '  docker-compose pull\n' +
                                '  docker-compose up -d\n' +
                                "' > '/etc/init.d/initScript.sh'\n" +
                                "_EOF_"
                        // set file for new session
                        sh '#!/bin/bash\n' +
                                'ssh ec2-user@pbm << _EOF_\n' +
                                "  sudo echo -e '#!/bin/bash\n" +
                                "  release_version=${CHOSEN_RELEASE}\n" +
                                "' > '/etc/profile.d/newSession.sh'\n" +
                                "_EOF_"
                        // restart docker containers
                        sh '#!/bin/bash\n' +
                                'ssh ec2-user@pbm << EOF\n' +
                                '  cd /home/ec2-user\n' +
                                '  docker-compose stop\n' +
                                '  docker-compose pull\n' +
                                '  docker-compose up -d\n' +
                                'EOF'
                    }
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

