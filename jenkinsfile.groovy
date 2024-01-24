pipeline {
    agent any

    environment {
        DOCKER_REGISTRY = "ezzatelshazly"
        DOCKER_IMAGE = "firstpipeline"
        imageTagApp = "build-${BUILD_NUMBER}-app"
        imageNameapp = "${DOCKER_REGISTRY}:${imageTagApp}"
        OPENSHIFT_PROJECT = 'ezzatelshazly'

    }

    stages {
        stage('Build Docker image for app.py and push it to docker hub') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub', usernameVariable: 'DOCKER_REGISTRY_USERNAME', passwordVariable: 'DOCKER_REGISTRY_PASSWORD')]) {
                    // Authenticate with Docker Hub to push Docker image
                    sh "echo \${DOCKER_REGISTRY_PASSWORD} | docker login -u \${DOCKER_REGISTRY_USERNAME} --password-stdin"
                    
                    // Build Docker image for app.py
                    sh "docker build -t ${imageNameapp} ."

                    sh "docker tag ${imageNameapp} docker.io/${DOCKER_REGISTRY}/${DOCKER_IMAGE}:${imageTagApp}"
                    
                    // Push the app Docker image to Docker Hub
                    sh "docker push docker.io/${DOCKER_REGISTRY}/${DOCKER_IMAGE}:${imageTagApp}"
                    
                    // Remove the locally built app Docker image
                    sh "docker rmi -f ${imageNameapp}"
                }
            }
        }


        stage('Deploy to OpenShift') {
            steps {
                script {
                    withCredentials([file(credentialsId: 'OCEE', variable: 'KUBECONFIG_FILE')]) {
                    sh "export KUBECONFIG=\$KUBECONFIG_FILE"
                        
                    // Replace the placeholder with the actual Docker image in the Kubernetes YAML files
                    sh "sed -i \'s|image:.*|image: ${DOCKER_REGISTRY}/${DOCKER_IMAGE}:${imageTagApp}|g\' ./deployment.yml"
                    
                    // Apply the deployment file
                    sh "oc apply -f deployment.yml -n ${OPENSHIFT_PROJECT}"
                
                }
            }
        }
    }
}
}