
node {
  checkout scm

  stage("build") {
    docker.image("smartcosmos/android-build:3").inside {
        sh "./gradlew clean build"
    }
  }
}
