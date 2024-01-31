def call(String imageNameApp, String DOCKER_REGISTRY, String DOCKER_IMAGE, String imageTagApp) {
    sh "chmod +x gradlew"
    sh "docker build -t ${imageNameApp} ."
    sh "docker tag ${imageNameApp} docker.io/${DOCKER_REGISTRY}/${DOCKER_IMAGE}:${imageTagApp}"
}
