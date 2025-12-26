pipeline {
    agent any

    environment {
        // Windows: ensure docker-compose is in PATH or specify full path
        DOCKER_COMPOSE_CMD = "docker-compose"
    }

    stages {
        stage('Checkout') {
            steps {
                // Checkout code from the new branch
                git branch: 'pipeline-2', url: 'https://github.com/YoussefBahaddou/MobileSec-MS'
                echo 'Checked out CI-CD-Pipleline successfully!'
            }
        }

        stage('Cleanup') {
            steps {
                // Run the robust cleanup script
                // using "powershell" directly if available, or bat calling powershell
                bat 'powershell -ExecutionPolicy Bypass -File scripts/ci_cleanup.ps1'
            }
        }

        stage('Build & Test Backend (Java)') {
            steps {
                script {
                    // Windows: Use backslashes for paths in some contexts, but dir() works with forward slashes
                    def javaServices = ['backend/gateway-service', 'backend/crypto-check', 'backend/ReportGen', 'backend/fix-suggest']
                    javaServices.each { service ->
                        dir(service) {
                            echo "Building ${service}..."
                            // Added -B for batch mode (less logs) and suppressed transfer progress
                            bat 'mvn -B -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn clean package -DskipTests'
                        }
                    }
                }
            }
        }
        
        stage('Docker Build & Deploy') {
            steps {
                echo 'Building and Starting Docker Containers...'
                bat "${DOCKER_COMPOSE_CMD} up -d --build"
            }
        }
        
        stage('Health Check') {
            steps {
                sleep 30
                bat "${DOCKER_COMPOSE_CMD} ps"
            }
        }
    }

    post {
        always {
            echo 'Pipeline completed.'
        }
        success {
            echo 'Deployment Successful! Access Frontend at http://localhost:3000'
        }
        failure {
            echo 'Pipeline Failed.'
        }
    }
}
