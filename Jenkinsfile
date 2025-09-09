pipeline {
  agent any
  options { timestamps(); ansiColor('xterm') }
  triggers { pollSCM('H/5 * * * *') } // Poll every ~5 minutes
  stages {
    stage('Checkout') {
      steps {
        git branch: 'main', url: 'https://github.com/Sandara-Git/8.2CDevSecOps.git'
      }
    }
    stage('Install Dependencies') {
      steps { sh 'npm install' }
    }
    stage('Run Tests') {
      steps { sh 'npm test || true' } // continue even if tests fail (per brief)
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
    }
  }
}
