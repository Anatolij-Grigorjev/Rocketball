package lt.mediapark.rocketball

import grails.transaction.Transactional
import lt.mediapark.rocketball.utils.DistanceCalc

@Transactional
class EventService {

  Event saveEvent(def params) {
    new Event(params).save(flush: true)
  }

  Event getClosestEvent(def lat, def lng) {
    //check for user location in an event type place
    def possibleEvents = Event.findAllByEventStartLessThanEqualsAndEventEndGreaterThanEquals(new Date(), new Date())
    def firstEvent = possibleEvents.find {
      DistanceCalc.getHavershineDistance(it.eventLat, it.eventLng, lat, lng) < it.eventRadius
    }

    firstEvent
  }
}
