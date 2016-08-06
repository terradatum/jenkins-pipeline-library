package com.terradatum.jenkins.workflow

class Version {
  int major, minor, patch
  String buildMetadata

  String toVersionString() {
    "${major}.${minor}.${patch}${buildMetadata}"
  }
}