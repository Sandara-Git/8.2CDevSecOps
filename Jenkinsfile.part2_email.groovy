pipeline {
  agent any
  options { timestamps() }
  triggers { pollSCM('H/5 * * * *') }
  environment { EMAIL_RECIPIENTS = 'sandarawijethunga4@gmail.com' } // change if needed
  stages {
    stage('Checkout') {
      steps {
        git branch: 'main', url: 'https://github.com/Sandara-Git/8.2CDevSecOps.git'
      }
    }
    stage('Install Dependencies') { steps { sh 'npm install' } }
    stage('Run Tests') {
      steps { sh 'npm test || true' }
      post {
        success {
          emailext(
            subject: "Tests passed — ${env.JOB_NAME} #${env.BUILD_NUMBER}",
            body: "Run Tests succeeded.\nBuild: ${env.BUILD_URL}",
            to: "${env.EMAIL_RECIPIENTS}",
            attachLog: true, compressLog: true
          )
        }
        failure {
          emailext(
            subject: "Tests failed — ${env.JOB_NAME} #${env.BUILD_NUMBER}",
            body: "Run Tests failed.\nBuild: ${env.BUILD_URL}",
            to: "${env.EMAIL_RECIPIENTS}",
            attachLog: true, compressLog: true
          )
        }
      }
    }
    stage('Generate Coverage Report') {
      steps {
        sh 'npm run coverage || true'
        archiveArtifacts artifacts: 'coverage/**', allowEmptyArchive: true
      }
    }
    stage('NPM Audit (Security Scan)') {
      steps {
        sh 'npm audit --json > npm-audit.json || true'
        archiveArtifacts artifacts: 'npm-audit.json', allowEmptyArchive: true
      }
      post {
        always {
          emailext(
            subject: "NPM Audit — ${env.JOB_NAME} #${env.BUILD_NUMBER}",
            body: "NPM Audit completed.\nBuild: ${env.BUILD_URL}\nAttached: npm-audit.json + build log.",
            to: "${env.EMAIL_RECIPIENTS}",
            attachLog: true, compressLog: true,
            attachmentsPattern: "npm-audit.json"
          )
        }
      }
    }
  }
}
