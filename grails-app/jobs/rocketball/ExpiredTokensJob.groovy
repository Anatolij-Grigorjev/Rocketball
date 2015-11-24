package rocketball
/**
 * Created by anatolij on 23/11/15.
 */
class ExpiredTokensJob {

    def execute() {
        def managers = [apnsManager, apnsManager.otherManager]
        managers.each {
            if (it) {
                log.info("Checking for expired tokens on manager ${it.name}")
                it.requestExpiredTokens()
            }
        }
    }

}
