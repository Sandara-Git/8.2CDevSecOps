pipeline {
  agent any
  options { timestamps() }
  triggers { pollSCM('H/5 * * * *') }

  environment {
    EMAIL_TO   = 's224740132@deakin.edu.au'       // change if needed
    EMAIL_FROM = 'sandarawijethunga4@gmail.com'   // must match the Gmail account you authenticated
  }

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
      steps {
        // If you want failure emails to trigger on test failure, REMOVE "|| true"
        sh 'npm test || true'
      }
      post {
        success {
          emailext(
            subject: "Tests passed — ${env.JOB_NAME} #${env.BUILD_NUMBER}",
            body: """Hello,

The *Run Tests* stage SUCCEEDED.
Build URL: ${env.BUILD_URL}
""",
            to:   "${env.EMAIL_TO}",
            from: "${env.EMAIL_FROM}",
            attachLog: true,
            compressLog: true
          )
        }
        failure {
          emailext(
            subject: " Tests failed — ${env.JOB_NAME} #${env.BUILD_NUMBER}",
            body: """Hello,

The *Run Tests* stage FAILED.
Build URL: ${env.BUILD_URL}
""",
            to:   "${env.EMAIL_TO}",
            from: "${env.EMAIL_FROM}",
            attachLog: true,
            compressLog: true
          )
        }
        // If you want an email regardless of result, add:
        // always {
        //   emailext( subject: "Tests finished — ${env.JOB_NAME} #${env.BUILD_NUMBER}", ... )
        // }
      }
    }

    stage('Generate Coverage Report') {
      steps {
        // Only run coverage if a script exists; otherwise skip cleanly
        sh '''
          if npm run | grep -q "^ *coverage"; then
            npm run coverage || true
          else
            echo "No coverage script found; skipping coverage."
          fi
        '''
        script {
          if (fileExists('coverage')) {
            archiveArtifacts artifacts: 'coverage/**', allowEmptyArchive: true
          } else {
            echo 'No coverage folder to archive.'
          }
        }
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
            subject: " NPM Audit — ${env.JOB_NAME} #${env.BUILD_NUMBER}",
            body: """Hello,

The *NPM Audit* stage completed.
Build URL: ${env.BUILD_URL}

Attached: npm-audit.json and compressed console log.
""",
            to:   "${env.EMAIL_TO}",
            from: "${env.EMAIL_FROM}",
            attachLog: true,
            compressLog: true,
            attachmentsPattern: "npm-audit.json"
          )
        }
      }
    }
  }
}
