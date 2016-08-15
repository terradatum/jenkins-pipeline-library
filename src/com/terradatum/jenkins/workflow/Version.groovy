package com.terradatum.jenkins.workflow

class Version {
  int major, minor, patch
  String buildMetadata

  Version(int major, int minor, int patch, String buildMetadata) {
    this.major = major
    this.minor = minor
    this.patch = patch
    this.buildMetadata = buildMetadata
  }

  String toVersionString() {
    "${major}.${minor}.${patch}+${buildMetadata}"
  }
}