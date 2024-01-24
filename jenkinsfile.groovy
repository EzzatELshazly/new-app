pipeline {
    agent any

    environment {
        
        DOCKER_REGISTRY = "ezzatelshazly"
        DOCKER_IMAGE = "firstpipeline"
        imageTagApp = "build-${BUILD_NUMBER}-app"
        imageNameapp = "${DOCKER_REGISTRY}:${imageTagApp}"
        OPENSHIFT_PROJECT = 'ezzatelshazly'
        GITHUB_REPO = "EzzatELshazly/new-app"
        OPENSHIFT_SERVER = 'https://api.ocpuat.devopsconsulting.org:6443'
        APP_SERVICE_NAME = 'servicePipelineEE'
        APP_PORT = '8080'
        APP_HOST_NAME = 'servicePipelineEE.apps.ocpuat.devopsconsulting.org'
        }

    stages {
        
        
        stage('Checkout') {
            steps {
                git url: "https://github.com/${GITHUB_REPO}.git", branch: 'main'
            }
        }
        
        stage('Build Docker image for app.py and push it to docker hub') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub', usernameVariable: 'DOCKER_REGISTRY_USERNAME', passwordVariable: 'DOCKER_REGISTRY_PASSWORD')]) {
                   
                    sh "echo \${DOCKER_REGISTRY_PASSWORD} | docker login -u \${DOCKER_REGISTRY_USERNAME} --password-stdin"
                    
                    sh "docker build -t ${imageNameapp} ."

                    sh "docker tag ${imageNameapp} docker.io/${DOCKER_REGISTRY}/${DOCKER_IMAGE}:${imageTagApp}"
                    
                    sh "docker push docker.io/${DOCKER_REGISTRY}/${DOCKER_IMAGE}:${imageTagApp}"
                    
                    sh "docker rmi -f ${imageNameapp}"
                }
            }
        }


        stage('Deploy to OpenShift') {
            steps {
                script {
                    withCredentials([string(credentialsId: 'Openshift2', variable: 'OPENSHIFT_SECRET')]) {
                    sh "oc login --token=\${OPENSHIFT_SECRET} \${OPENSHIFT_SERVER} --insecure-skip-tls-verify"
                    }
                    sh "oc project \${OPENSHIFT_PROJECT}"
                    sh "oc new-app \${DOCKER_REGISTRY}/${DOCKER_IMAGE}:${BUILD_NUMBER}"
                    sh "oc apply -f deployment.yml "
                    sh "oc create route edge --service \${APP_SERVICE_NAME} --port \${APP_PORT} --hostname \${APP_HOST_NAME} --insecure-policy Redirect"
                }
            }
        }
}
}