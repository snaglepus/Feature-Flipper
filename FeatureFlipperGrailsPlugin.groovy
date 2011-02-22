import org.codehaus.groovy.grails.commons.*
import es.robjam.Feature


class FeatureFlipperGrailsPlugin {
  // the plugin version
  def version = "0.1"
  // the version or versions of Grails the plugin is designed for
  def grailsVersion = "1.3.5 > *"
  // the other plugins this plugin depends on
  def dependsOn = [controllers: "1.3.5 > *", services: "1.3.5 > *", logging: "1.3.5 > *"]
  def loadAfter = ['controllers', 'services']

  def observe = ['controllers', 'services']

  // resources that are excluded from plugin packaging
  def pluginExcludes = [
          "grails-app/views/error.gsp"
  ]

  // TODO Fill in these fields
  def author = "Rob James"
  def authorEmail = "james.rob@gmail.com"
  def title = "Feature Flipping per server"
  def description = '''\\
Provides a mechanism to be developing features and deploying them into production and 'flipping' the features on or off.
Feature definitions are stored in an XML file and methods are provided to manage this file. Supports configuration per
server as well as supporting security Roles.
'''

  def watchedResources = ["file:./grails-app/services/*Service.groovy", "file:./grails-app/services/*Controller.groovy"]

  // URL to the plugin's documentation
  def documentation = "http://grails.org/plugin/feature-flipper"

  def doWithWebDescriptor = { xml ->
    // TODO Implement additions to web.xml (optional), this event occurs before
  }

  def doWithSpring = {
    // TODO Implement runtime spring config (optional)
  }

  def doWithDynamicMethods = { applicationContext2 ->
    // TODO Implement registering dynamic methods to classes (optional)

    addMethods(application, applicationContext)
  }
  //}

  def doWithApplicationContext = { applicationContext ->
    // TODO Implement post initialization spring config (optional)
    // Add the ability to load the Feature Flipper Config file into the application settings
    readConfigFile(applicationContext)

  }

  def onChange = { event ->
    // TODO Implement code that is executed when any artefact that this plugin is
    // watching is modified and reloaded. The event contains: event.source,
    // event.application, event.manager, event.ctx, and event.plugin.
//    GrailsApplication  application = (GrailsApplication) applicationContext.getBean("grailsApplication")
    addMethods(application, applicationContext)
  }

  def onConfigChange = { event ->
    // TODO Implement code that is executed when the project configuration changes.
    // The event is the same as for 'onChange'.
  }


  private addMethods(application, applicationContext) {
    application.controllerClasses.each { controllerClass ->

      /*
        Method that allows testing whether a feature is active and that it is turned on or active. Requires passing the
        name of the feature that is being tested
       */
      controllerClass.metaClass.hasFeature = {featureName ->
        def featureActive = false
        def feature = applicationContext.servletContext?.features.find {it.name.toString() == featureName.toString()}

        if (feature?.active) featureActive = true
        return featureActive
      }

      /*
        Method that in addition to testing whether the feature is turned on and active, it also makes sure that it is
        active for the roles that are passed. Requires passing the name of the feature that is being tested as well as
        the ROLES that exist for this session. If any of the Roles passed in exist in the feature, then it will pass.
        ROLES is an ArrayList of Strings
       */
      controllerClass.metaClass.hasFeature = {featureName, validRoles ->
        def featureActive = false
        def feature = applicationContext.servletContext?.features.find {it.name.toString() == featureName.toString()}

        if (feature?.active) {
          feature?.roles.each {
            if (validRoles.contains(it)) featureActive = true
          }
        }
        return featureActive
      }
    }

    application.serviceClasses.each { serviceClass ->

      /*
        Method that allows testing whether a feature is active and that it is turned on or active. Requires passing the
        name of the feature that is being tested
       */
      serviceClass.metaClass.hasFeature = {featureName ->
        def featureActive = false
        def feature = applicationContext.servletContext?.features.find {it.name.toString() == featureName.toString()}

        if (feature?.active) featureActive = true
        return featureActive
      }

      /*
        Method that in addition to testing whether the feature is turned on and active, it also makes sure that it is
        active for the roles that are passed. Requires passing the name of the feature that is being tested as well as
        the ROLES that exist for this session. If any of the Roles passed in exist in the feature, then it will pass.
        ROLES is an ArrayList of Strings
       */
      serviceClass.metaClass.hasFeature = {featureName, validRoles ->
        def featureActive = false
        def feature = applicationContext.servletContext?.features.find {it.name.toString() == featureName.toString()}

        if (feature?.active) {
          feature?.roles.each {
            if (validRoles.contains(it)) featureActive = true
          }
        }
        return featureActive
      }
    }
  }


  private readConfigFile(application) {
    def config = ConfigurationHolder.config

    def flipperConfigPath = (config.featureFlipper.flipperConfigPath) ?: "FeatureFlipper.xml"

    if (new File(flipperConfigPath).exists()) {
      //read a config file
      loadConfig(new File(flipperConfigPath).text, application)
    } else {
      //create an Empty file
      new File(flipperConfigPath).append("<features></features>")
    }
  }

  private loadConfig(config, application) {
    def content = parseConfig(config)
    application.servletContext.features = []

    content.featureitem.each { feature ->
      //feature must have a name and it must be unique
      if (!application.servletContext.features.collect {it.name}.contains(feature?.name?.toString()) && feature?.name && feature?.name != "" ) {
        def f = new Feature()
        f.name = feature?.name?.toString()
        f.description = feature?.description?.toString()
        f.active = (feature?.active == "true") ? true : false
        if (feature?.roles) {
          f.roles = []
          feature?.roles.role.each {
            f.roles << it.toString()
          }
        }
        application.servletContext.features << f
      }
    }
  }

  private parseConfig(config) {
    try {
      def parsed = new XmlSlurper().parseText(config)
      return parsed
    } catch (Exception e) {
      log.error "Unable to Parse Feature Flipper Configuration File"
    }
  }
}
