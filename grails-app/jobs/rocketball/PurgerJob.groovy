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
            //when users are first registered, the timestamp here is 0,
            //need to make sure not to purge those on the next take
            ids.findAll { it.value > 0 }.each {
                //too much time has passed, no more coords for this person
                if (Math.abs(now - it.value) > Constants.TTL_PERIOD_MS) {
                    //returns boolean true if actually cleared some shit
                    def didIt = userService.clearCoords(it.key)
                    if (didIt) {
                        purges++
                    }
                }
            }
            if (purges) {
                log.info("${Environment.current == Environment.DEVELOPMENT ? "Skipped" : "Purged"} ${purges} user coordinates")
            }
        }
    }
}
