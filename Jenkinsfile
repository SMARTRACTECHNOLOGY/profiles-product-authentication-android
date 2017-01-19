
node {
  checkout scm

  stage("build") {
    docker.image("smartcosmos/android-build:2").inside {
        sh "./gradlew clean build"
    }
  }
}
