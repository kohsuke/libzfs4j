node('docker') {
    checkout scm
    docker.image('maven:3.3.9-jdk-8').inside {
        sh 'mvn clean verify'
    }
}
