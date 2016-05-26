package rocketball

import grails.util.Environment
import grails.util.Holders
import lt.mediapark.rocketball.UserService
import lt.mediapark.rocketball.utils.Constants
import org.quartz.Job
import org.quartz.JobExecutionContext


class PurgerJob implements Job {

  void execute(JobExecutionContext jobCtx) {
    def userService = (UserService) Holders.grailsApplication.getServiceClass('userService')
    //check if service injected yet
    if (!userService) {
      return
    }
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
