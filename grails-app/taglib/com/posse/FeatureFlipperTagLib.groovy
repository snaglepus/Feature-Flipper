package com.posse

class FeatureFlipperTagLib {
  static namespace = "ff"
  def featureFlipperService
  /**
   * Checks to see if a feature is enabled. If the feature cannot be found, it is ignored and considered enabled.
   *
   * @attr name REQUIRED The name of the feature that is being tested
   * @attr roles An ArrayList of roles the current user has if this is also being tested
   */
  def hasFeature = { attrs, body ->
    def name = attrs?.name
    def roles = attrs?.roles
    def hasFeature = true

    if (name && roles) {
      //name and role passed - test it
      hasFeature = featureFlipperService.hasFeature(name, roles)
    } else if (name) {
      //only name passed - test it
      hasFeature = featureFlipperService.hasFeature(name)
    }

    if (hasFeature) {
      out << body()
    }
  }
}
