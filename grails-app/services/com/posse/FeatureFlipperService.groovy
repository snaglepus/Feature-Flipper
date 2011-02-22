package com.posse

import org.codehaus.groovy.grails.web.context.ServletContextHolder as SCH
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import groovy.xml.*
import es.robjam.Feature

class FeatureFlipperService {
  static transactional = false
  def servletContext = SCH.servletContext

  /*
   Method that returns an array of the existing features. Requires no params
  */
  def listFeatures() {
    return servletContext?.features
  }

  /*
    Method that returns the feature object. Requires the feature name
   */
  def getFeature(name) {
    return servletContext?.features.find {it.name.toString() == name.toString()}
  }

  /*
    Method to add a new Feature. This add it to the XML and as well as the application object so that it is
    persisted as well as immediately available to the application.
   */
  def setFeature(feature) {
    //get the name and take out the spaces
    if (feature?.name?.trim() == "") {
      return null
    }

    if (feature?.name) {
      feature?.name = feature?.name?.toString().replaceAll(/\s|\W/, "")
    }

    //if the feature exists, update it, otherwise add a new one.
    def f = servletContext?.features.find {it.name.toString() == feature?.name?.toString()}
    if (f) {
      //update existing
      f.description = (feature?.description?.toString()) ?: f.description
      f.active = feature?.active
      if (f.active.class != Boolean) {
        f.active = false
      }
      if (feature?.roles?.class == java.util.ArrayList) {
        f.roles = feature?.roles
      } else {
        f.roles = []
      }
    } else {
      //create new
      def newFeature = new Feature()
      newFeature.name = feature?.name?.toString()
      newFeature.description = feature?.description?.toString()
      newFeature.active = (feature?.active) ?: false
      feature?.roles?.each {
        newFeature.roles << it
      }
      if (!servletContext?.features) {
        servletContext?.features = []
      }
      servletContext?.features << newFeature
      updateFeaturesXML()
    }
  }

  /*
   Method to remove a feature from the featureList
  */
  def removeFeature(featureName) {
    def f = servletContext?.features.find {it.name.toString() == featureName?.toString()}
    if (f) {
      servletContext?.features.remove(f)
      updateFeaturesXML()
    } else {
      log.error "Feature cannot be found in Feature List"
    }
  }


  private updateFeaturesXML() {
    try {
      def sw = new StringWriter()
      def xml = new MarkupBuilder(sw)
      xml.features {
        servletContext.features.each { f ->
          featureitem {
            name(f.name)
            description(f.description)
            active(f.active)
            roles {
              f.roles.each { r ->
                role(r)
              }
            }
          }
        }
      }

      //save the xml file
      def config = ConfigurationHolder.config
      def flipperConfigPath = (config.featureFlipper.flipperConfigPath) ?: "FeatureFlipper.xml"
      new File(flipperConfigPath).write(sw.toString())
    } catch (Exception e) {
      log.error "Unable to write the FeatureFlipper XML file: ${e}"
    }
  }
}

