import com.google.android.gcm.server.Message
import com.google.android.gcm.server.Sender
import com.relayrides.pushy.apns.ApnsEnvironment
import com.relayrides.pushy.apns.ApnsPushNotification
import com.relayrides.pushy.apns.PushManager
import com.relayrides.pushy.apns.PushManagerConfiguration
import com.relayrides.pushy.apns.util.ApnsPayloadBuilder
import com.relayrides.pushy.apns.util.SSLContextUtil
import com.relayrides.pushy.apns.util.SimpleApnsPushNotification
import com.relayrides.pushy.apns.util.TokenUtil
import grails.util.Environment
import lt.mediapark.rocketball.utils.Constants
import rocketball.EmitterJob
import rocketball.PurgerJob

import javax.net.ssl.SSLHandshakeException
import java.util.concurrent.atomic.AtomicLong

class BootStrap {

    private static final AtomicLong imageIndexer = new AtomicLong(1)
    def grails
    def grailsApplication

    int prints = 0
    boolean printedN = false

    PushManager pushManagerDev
    PushManager pushManagerProd

    Sender gcmSender

    def init = { servletContext ->

        Constants.init(grailsApplication.config.grails)

        PurgerJob.schedule(Constants.TTL_PERIOD_MS)
        if (Environment.DEVELOPMENT == Environment.current) {
            EmitterJob.schedule(Constants.HEARTBEAT_PERIOD_MS)
        }
        grailsApplication.allArtefacts.each {
            it.metaClass.downloadImage = { String address ->
                if (!address) {
                    return null
                }
                def name = "image-${imageIndexer.incrementAndGet()}.png"
                //this returns an output stream
                def file = File.createTempFile(name, '')
                file.bytes = new URL(address).bytes
                file
            }
        }
        grails = grailsApplication.config.grails

        //APNS
        pushManagerDev = buildPushy(grails, 'dev')
        pushManagerProd = buildPushy(grails, 'prod')
        pushManagerDev?.metaClass?.otherManager = pushManagerProd
        registerListeners(pushManagerDev)
        registerListeners(pushManagerProd)
        if (pushManagerProd) {
            pushManagerProd?.start()
            log.info("Push manager ${pushManagerProd?.name} initialized!")
        }
        if (pushManagerDev) {
            pushManagerDev?.start()
            log.info("Push manager ${pushManagerDev?.name} initialized!")
        }
        grailsApplication.allArtefacts.each { klass -> addApnsMethods(klass) }


        //GCM
        gcmSender = new Sender(grails.gcm.browser.key)
        if (gcmSender) {
            log.info("GCM manager ${gcmSender?.toString()} initialized!")
        }
        grailsApplication.allArtefacts.each { klass -> addGCMMethods(klass) }
    }

    private PushManager buildPushy(ConfigObject grails, String env) {
        def managerConfig = new PushManagerConfiguration()
        def apnsEnv = ApnsEnvironment."${grails.apns."${env}".environment}Environment"
        try {
            def sslCtx
            try {
                sslCtx = SSLContextUtil.createDefaultSSLContext((String) grails.apns."${env}".p12.path,
                        (String) grails.apns."${env}".p12.password)
            } catch (FileNotFoundException e) {
                log.error "File not found at ${e.message}, using alternative location..."
                sslCtx = SSLContextUtil.createDefaultSSLContext((String) grails.apns."${env}".p12.local.path,
                        (String) grails.apns."${env}".p12.password)
            }
            def pushManager = new PushManager<ApnsPushNotification>(
                    apnsEnv,
                    sslCtx,
                    null, //event loop group
                    null, //executor service
                    null, //blocking queue
                    managerConfig,
                    (String) grails.apns."${env}".manager.name);
            return pushManager
        } catch (Exception e) {
            log.debug('Problem with APNS config: ' + e.message)
            e.printStackTrace()
        }
    }

    def addGCMMethods(Class klass) {
        klass.metaClass.static.gcmSender = gcmSender
        klass.metaClass.static.sendGCMNotification = { String regId, Closure builder ->
            log.debug("Sending to regId ${regId}")

            Message.Builder msgBuild = new Message.Builder()
            builder(msgBuild)
            def message = msgBuild.build()
            log.debug("Built message: ${message}")

            def result = gcmSender.sendNoRetry(message, regId)
            log.info("GCM Send result: ${result}")
        }

    }

    def addApnsMethods(def klass) {
        klass.metaClass.static.apnsManager = pushManagerDev;
        klass.metaClass.static.viaApns = { Closure actions ->
            log.debug('Invoking APNS manager ' + pushManagerDev.name)
            actions(pushManagerDev);
            log.debug('Done with via APNS')
        }

        klass.metaClass.static.sendAPNSNotification = { token, builder ->
            log.debug("Transalting token ${token} to bytes")
            def tokenBytes = TokenUtil.tokenStringToByteArray(token)
            log.trace("token bytes ${tokenBytes}")
            ApnsPayloadBuilder payloadBuilder = new ApnsPayloadBuilder()
            log.trace('Applying payload builder...')
            builder(payloadBuilder)

            def payload = payloadBuilder.buildWithDefaultMaximumLength()

            log.trace("Created payload: ${payload}")

            viaApns { apns ->
                def q = apns.getQueue()
                log.info("Got Q: ${q}\n(total: ${q.size()})")
                q.put(new SimpleApnsPushNotification(tokenBytes, payload))
                log.info('Message put in q!')
            }

            log.trace('Done sending notification')
        }

        //classes with device token saved in them can curry the apns closure
        if (klass.metaClass.hasProperty('deviceToken')) {
            klass.metaClass.registerDeviceToken = { token ->
                klass.metaClass.pushNotification = sendAPNSNotification.curry(token)
            }
        }
    }


    def registerListeners = { PushManager theManager ->

        theManager?.registerRejectedNotificationListener { manager, notification, reason ->
            log.error "Notification ${notification} from manager ${manager.name} was rejected for reason ${reason}."
            if (manager.hasProperty('otherManager')) {
                PushManager prodPushy = manager.otherManager
                if (prodPushy) {
                    log.info("Trying the other manager, ${prodPushy.name}!")
                    def q = prodPushy.getQueue()
                    log.debug("Got q: ${q}")
                    if (q != null) {
                        q.put(notification)
                        log.debug('Message put in q! Done with 2nd manager!')
                    }
                }
            }
        }

        theManager?.registerFailedConnectionListener { manager, cause ->
            if (!printedN) {
                log.error("${manager.name} failed connection because of ${cause}!", cause)
                cause.printStackTrace()
                prints++
                printedN = prints > 7
            }
            if (cause instanceof SSLHandshakeException) {
                //need to shutdown manager since no more SSL
                log.fatal "SSL Certificate expired/invalid (${cause.message})! Shutting down push service..."
                pushManagerDev.shutdown()
            }
        }

        theManager?.registerExpiredTokenListener { manager, expiredTokens ->
            log.info "These are the tokens received after expry: ${expiredTokens}"
        }

    }

    def destroy = {
        if (pushManagerDev?.isStarted()) {
            pushManagerDev.shutdown()
            //sleep after shutting down PUSHY to not leave Netty memory leaks
            //see https://github.com/relayrides/pushy/issues/29
            Thread.sleep(2500)
        }
    }

}
