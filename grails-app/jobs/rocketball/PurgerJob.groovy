package rocketball

import grails.util.Environment
import lt.mediapark.rocketball.utils.Constants


class PurgerJob {

    def userService

//    static triggers = {
//        simple repeatInterval: Constants.TTL_PERIOD_MS //once every 8 seconds
//    }

    def execute() {
        if (userService.loggedInUsers) {
            Map<Long, Long> ids = [:] << userService.loggedInUsers
            int purges = 0
            long now = new Date().time
            ids.each {
                //too much time has passed, no more coords for this person
                if (now - it.value > Constants.TTL_PERIOD_MS) {
                    userService.clearCoords(it.key)
                    purges++
                }
            }
            log.info("${Environment.current == Environment.DEVELOPMENT ? "Skipped" : "Purged"} ${purges} user coordinates")
        }
    }
}
