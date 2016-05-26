import org.quartz.*
import rocketball.PurgerJob

// locations to search for config files that get merged into the main config;
// config files can be ConfigSlurper scripts, Java properties files, or classes
// in the classpath in ConfigSlurper format

// grails.config.locations = [ "classpath:${appName}-config.properties",
//                             "classpath:${appName}-config.groovy",
//                             "file:${userHome}/.grails/${appName}-config.properties",
//                             "file:${userHome}/.grails/${appName}-config.groovy"]

// if (System.properties["${appName}.config.location"]) {
//    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
// }

grails.project.groupId = appName // change this to alter the default package name and Maven publishing destination

// The ACCEPT header will not be used for content negotiation for user agents containing the following strings (defaults to the 4 major rendering engines)
grails.mime.disable.accept.header.userAgents = ['Gecko', 'WebKit', 'Presto', 'Trident']
grails.mime.types = [ // the first one is the default format
                      all          : '*/*', // 'all' maps to '*' or the first available format in withFormat
                      atom         : 'application/atom+xml',
                      css          : 'text/css',
                      csv          : 'text/csv',
                      form         : 'application/x-www-form-urlencoded',
                      html         : ['text/html', 'application/xhtml+xml'],
                      js           : 'text/javascript',
                      json         : ['application/json', 'text/json'],
                      multipartForm: 'multipart/form-data',
                      rss          : 'application/rss+xml',
                      text         : 'text/plain',
                      hal          : ['application/hal+json', 'application/hal+xml'],
                      xml          : ['text/xml', 'application/xml']
]

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

// Legacy setting for codec used to encode data with ${}
grails.views.default.codec = "html"

// The default scope for controllers. May be prototype, session or singleton.
// If unspecified, controllers are prototype scoped.
grails.controllers.defaultScope = 'singleton'

// GSP settings
grails {
  views {
    gsp {
      encoding = 'UTF-8'
      htmlcodec = 'xml' // use xml escaping instead of HTML4 escaping
      codecs {
        expression = 'html' // escapes values inside ${}
        scriptlet = 'html' // escapes output from scriptlets in GSPs
        taglib = 'none' // escapes output from taglibs
        staticparts = 'none' // escapes output from static template parts
      }
    }
    // escapes all not-encoded output at final stage of outputting
    // filteringCodecForContentType.'text/html' = 'html'
  }
}


grails.converters.encoding = "UTF-8"
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []
// whether to disable processing of multi part requests
grails.web.disable.multipart = false

// request parameters to mask when logging exceptions
grails.exceptionresolver.params.exclude = ['password']

// configure auto-caching of queries by default (if false you can cache individual queries with 'cache: true')
grails.hibernate.cache.queries = false

// configure passing transaction's read-only attribute to Hibernate session, queries and criterias
// set "singleSession = false" OSIV mode in hibernate configuration after enabling
grails.hibernate.pass.readonly = false
// configure passing read-only to OSIV session by default, requires "singleSession = false" OSIV mode
grails.hibernate.osiv.readonly = false
// make all properties nullable by default
grails.gorm.default.constraints = {
  '*'(nullable: true)
}
grails.gorm.default.mapping = {
  dynamicUpdate(true)
  version(false)
}

//APNS config for dev pushing with prod fallback

environments {
  development {
    grails.apns.dev.p12.path = '/opt/tomcat-rocketball-test/cert/rocketball_dev.p12'
    grails.apns.dev.p12.local.path = '/Users/anatolij/Documents/RocketBallDev.p12'
    grails.apns.prod.p12.path = '/opt/tomcat-rocketball-test/cert/rocketball_prod.p12'
    grails.apns.prod.p12.local.path = '/Users/anatolij/Documents/RocketBallProd.p12'
  }
  production {
    grails.apns.dev.p12.path = '/opt/tomcat-rocketball/cert/rocketball_dev.p12'
    grails.apns.prod.p12.path = '/opt/tomcat-rocketball/cert/rocketball_prod.p12'
  }
}

grails.apns.dev.p12.password = 'RocketBall2015'
grails.apns.dev.manager.name = 'DEV-PUSHY-MANAGER'
grails.apns.dev.environment = 'sandbox'
grails.apns.prod.p12.password = 'RocketBall2015'
grails.apns.prod.manager.name = 'PROD-PUSHY-MANAGER'
grails.apns.prod.environment = 'production'

//GCM config for pushing to android devices

grails.gcm.browser.key = 'AIzaSyB0gZsNPWmTVFqw8hZ36Vt3DwHTkmWWfYE'

// turn off silent GORM errors
grails.gorm.failOnError = true
//quartz config locations
grails.config.locations = ["file:Quartz-config.groovy"]

environments {
  development {
    grails.logging.jul.usebridge = true
  }
  production {
    grails.logging.jul.usebridge = false
    // TODO: grails.serverURL = "http://www.changeme.com"
  }
}

grails.rocketball.radius = 1000.0 // radius in meters
grails.rocketball.mindist = 20.5 //minimum distance to walk from last position before coordinates get updated
grails.rocketball.apns.tokens_check = 6000000 //every 10 hours is enough

environments {
  development {
    grails.rocketball.heartbeat = 25000 //25 seconds in ms
    grails.rocketball.ttl = 300000 //5 min in ms
  }
  production {
    grails.rocketball.heartbeat = 25000 //25 seconds in ms
    grails.rocketball.ttl = 600000 //10 min in ms
  }
}

grails {
  mail {
    host = "rododendras.serveriai.lt"
    port = 465
    username = "support@rocketballapp.com"
    password = "UzneikisDeselioti"
    props = ["mail.smtp.auth"                  : "true",
             "mail.smtp.socketFactory.port"    : "465",
             "mail.smtp.socketFactory.class"   : "javax.net.ssl.SSLSocketFactory",
             "mail.smtp.socketFactory.fallback": "false"]
  }
}

// log4j configuration
log4j.main = {
  // Example of changing the log pattern for the default console appender:
  //
  //appenders {
  //    console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
  //}
  environments {
    development {
      debug 'grails.app.controllers',        // controllers
        'grails.app.services',          //services
        'grails.app.filters',           //filters
        'grails.app'                    //bootstrap
    }
    production {
      info 'grails.app.controllers',        // controllers
        'grails.app.services',          //services
        'grails.app.filters',           //filters
        'grails.app'
    }
  }


  error 'org.codehaus.groovy.grails.web.servlet',        // controllers
    'org.codehaus.groovy.grails.web.pages',          // GSP
    'org.codehaus.groovy.grails.web.sitemesh',       // layouts
    'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
    'org.codehaus.groovy.grails.web.mapping',        // URL mapping
    'org.codehaus.groovy.grails.commons',            // core / classloading
    'org.codehaus.groovy.grails.plugins',            // plugins
    'org.codehaus.groovy.grails.orm.hibernate',      // hibernate integration
    'org.springframework',
    'org.hibernate',
    'net.sf.ehcache.hibernate'
}

//job to remove stale unverified users (that didn't respond to the emails)
grails.plugin.quartz2.jobSetup.staleUserCoordinatesPurgerJob = { quartzScheduler, ctx ->

  //wrap executor into job detail
  JobDetail jobDetail = JobBuilder.newJob(PurgerJob.class)
    .withIdentity('StaleCoordsCleaner')
  //5 minutes in millis - length of time the player has for their entry
//    .usingJobData(new JobDataMap([verificationTTL: pickmonster.user.verification.ttl]))
    .build()

  //create trigger to run this every 10 minutes
  Trigger trigger = TriggerBuilder.newTrigger()
    .withIdentity('StaleCoordsCleaner')
    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
    .withIntervalInMilliseconds(grails.rocketball.ttl)
    .repeatForever())
    .startNow()
    .build()

  //start the damned thing
  quartzScheduler.scheduleJob(jobDetail, trigger)
}