package com.terradatum.jenkins.workflow

import com.cloudbees.groovy.cps.NonCPS

@GrabResolver(name='nexus', root='https://nexus.terradatum.com/content/groups/public/')
@Grab('com.github.zafarkhaja:java-semver:0.10-SNAPSHOT')

/**
 * @author rbellamy@terradatum.com 
 * @date 8/21/16
 */
class Version implements Serializable {

  private static final long serialVersionUID = -2008891377046871665L;
  com.github.zafarkhaja.semver.Version delegate;

  private def Version(com.github.zafarkhaja.semver.Version version) {
    delegate = version
  }

  def Version(String versionString) {
    delegate = com.github.zafarkhaja.semver.Version.valueOf(versionString)
  }

  void setDelegate(com.github.zafarkhaja.semver.Version version) {
    this.delegate = version
  }

  boolean satisfies(String expr) {
    return delegate.satisfies(expr)
  }

  Version incrementMajorVersion() {
    delegate = delegate.incrementMajorVersion()
    return new Version(delegate)
  }

  Version incrementMajorVersion(String preRelease) {
    delegate = delegate.incrementMajorVersion(preRelease)
    return new Version(delegate)
  }

  Version incrementMinorVersion() {
    delegate = delegate.incrementMinorVersion()
    return new Version(delegate)
  }

  Version incrementMinorVersion(String preRelease) {
    delegate = delegate.incrementMinorVersion(preRelease)
    return new Version(delegate)
  }

  Version incrementPatchVersion() {
    delegate = delegate.incrementPatchVersion()
    return new Version(delegate)
  }

  Version incrementPatchVersion(String preRelease) {
    delegate = delegate.incrementPatchVersion(preRelease)
    return new Version(delegate)
  }

  Version incrementPreReleaseVersion() {
    delegate = delegate.incrementPreReleaseVersion()
    return new Version(delegate)
  }

  Version incrementBuildMetadata() {
    delegate = delegate.incrementBuildMetadata()
    return new Version(delegate)
  }

  Version setPreReleaseVersion(String preRelease) {
    delegate = delegate.setPreReleaseVersion(preRelease)
    return new Version(delegate)
  }

  Version setBuildMetadata(String build) {
    delegate = delegate.setBuildMetadata(build)
    return new Version(delegate)
  }

  int getMajorVersion() {
    return delegate.getMajorVersion()
  }

  int getMinorVersion() {
    return delegate.getMinorVersion()
  }

  int getPatchVersion() {
    return delegate.getPatchVersion()
  }

  String getNormalVersion() {
    return delegate.getNormalVersion()
  }

  String getPreReleaseVersion() {
    return delegate.getPreReleaseVersion()
  }

  String getBuildMetadata() {
    return delegate.getBuildMetadata()
  }

  boolean greaterThan(Version other) {
    return delegate.greaterThan(other.delegate)
  }

  boolean greaterThanOrEqualTo(Version other) {
    return delegate.greaterThanOrEqualTo(other.delegate)
  }

  boolean lessThan(Version other) {
    return delegate.lessThan(other.delegate)
  }

  boolean lessThanOrEqualTo(Version other) {
    return delegate.lessThanOrEqualTo(other.delegate)
  }

  @Override
  @NonCPS
  boolean equals(Object other) {
    return delegate == (other as Version).delegate
  }

  @Override
  @NonCPS
  int hashCode() {
    return delegate.hashCode()
  }

  @Override
  @NonCPS
  String toString() {
    return delegate.toString()
  }

  int compareTo(Version other) {
    return delegate <=> other.delegate
  }

  int compareWithBuildsTo(Version other) {
    return delegate.compareWithBuildsTo(other.delegate)
  }

  static Version valueOf(String versionString) {
    return new Version(versionString)
  }
}
