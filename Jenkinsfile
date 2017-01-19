
node {
  checkout scm

  stage("build") {
    docker.image("smartcosmos/android-build:4").inside {
        sh "./gradlew clean build"
    }
  }
}
