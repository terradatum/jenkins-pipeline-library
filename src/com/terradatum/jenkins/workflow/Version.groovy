package com.terradatum.jenkins.workflow

class Version implements Serializable {
  int major, minor, patch
  String buildMetadata

  String toVersionString() {
    "${major}.${minor}.${patch}${buildMetadata}"
  }
}