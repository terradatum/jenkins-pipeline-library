package com.terradatum.jenkins.workflow

import groovy.transform.ToString
@ToString
class Version implements Serializable {
  int major, minor, patch
  String buildMetadata

  String toVersionString() {
    "${major}.${minor}.${patch}${buildMetadata}"
  }
}