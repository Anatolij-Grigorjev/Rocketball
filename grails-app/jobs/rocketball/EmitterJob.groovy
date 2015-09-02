package rocketball

import grails.util.Environment
import lt.mediapark.rocketball.User

class EmitterJob {

    def userService

//    static triggers = {
//        simple repeatInterval: Constants.HEARTBEAT_PERIOD_MS
//    }
    def rnd = new Random()

    def execute() {
        if (Environment.current == Environment.DEVELOPMENT) {
            int emitts = 0
            User.findAll { userFbId < 0 }.each {
                emitts++
                coordsEmitter(it, 54.689566, 25.272500)
            }
            if (emitts) {
                log.debug("Emitted fresh coords for ${emitts} test users")
            }
        }
    }

    def coordsEmitter = { User user, Double latOrigin, Double lngOrigin ->
        def map = [:]
        map.lat = latOrigin - (rnd.nextDouble() / rnd.nextInt(10000))
        map.lng = lngOrigin + (rnd.nextDouble() / rnd.nextInt(10000))

        userService.updateCoords(user, map)
    }
}
