import lt.mediapark.rocketball.utils.Constants
import rocketball.EmitterJob
import rocketball.PurgerJob

import java.util.concurrent.atomic.AtomicLong

class BootStrap {

    private static final AtomicLong imageIndexer = new AtomicLong(1)

    def grailsApplication

    def init = { servletContext ->

        Constants.init(grailsApplication.config.grails)

        PurgerJob.schedule(Constants.TTL_PERIOD_MS)
        EmitterJob.schedule(Constants.HEARTBEAT_PERIOD_MS)

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

    }
    def destroy = {

    }
}
