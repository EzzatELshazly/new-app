def call() {
   withSonarQubeEnv() {
      sh "./gradlew sonar"
    }
}