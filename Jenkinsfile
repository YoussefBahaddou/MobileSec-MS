pipeline {
    agent any

    environment {
        // Windows: ensure docker-compose is in PATH or specify full path
        DOCKER_COMPOSE_CMD = "docker-compose"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                echo 'Code Checked Out successfully!'
            }
        }

        stage('Cleanup') {
            steps {
                // Windows Batch command to clean up
                // "call" is used to prevent the script from exiting early if one command fails
                bat "call ${DOCKER_COMPOSE_CMD} down --volumes --remove-orphans || echo No containers to remove"
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
                            // Assuming 'mvn' is in system PATH as per guide
                            bat 'mvn clean package -DskipTests'
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
