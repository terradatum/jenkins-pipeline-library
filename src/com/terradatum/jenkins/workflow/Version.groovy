package com.terradatum.jenkins.workflow

import com.cloudbees.groovy.cps.NonCPS

@GrabResolver(name='nexus', root='https://nexus.terradatum.com/content/groups/public/')
@Grab('com.github.zafarkhaja:java-semver:0.10-SNAPSHOT')

/**
 * @author rbellamy@terradatum.com 
 * @date 8/21/16
 */
class Version implements Serializable, Comparable {

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
    return new Version(delegate.incrementMajorVersion())
  }

  Version incrementMajorVersion(String preRelease) {
    return new Version(delegate.incrementMajorVersion(preRelease))
  }

  Version incrementMinorVersion() {
    return new Version(delegate.incrementMinorVersion())
  }

  Version incrementMinorVersion(String preRelease) {
    return new Version(delegate.incrementMinorVersion(preRelease))
  }

  Version incrementPatchVersion() {
    return new Version(delegate.incrementPatchVersion())
  }

  Version incrementPatchVersion(String preRelease) {
    return new Version(delegate.incrementPatchVersion(preRelease))
  }

  Version incrementPreReleaseVersion() {
    return new Version(delegate.incrementPreReleaseVersion())
  }

  Version incrementBuildMetadata() {
    return new Version(delegate.incrementBuildMetadata())
  }

  Version setPreReleaseVersion(String preRelease) {
    return new Version(delegate.setPreReleaseVersion(preRelease))
  }

  Version setBuildMetadata(String build) {
    return new Version(delegate.setBuildMetadata(build))
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

  @Override
  @NonCPS
  int compareTo(Object other) {
    return delegate <=> (other as Version).delegate
  }

  int compareWithBuildsTo(Version other) {
    return delegate.compareWithBuildsTo(other.delegate)
  }

  static Version valueOf(String versionString) {
    return new Version(versionString)
  }
}
