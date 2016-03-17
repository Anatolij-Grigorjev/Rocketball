package lt.mediapark.rocketball

import grails.transaction.Transactional
import lt.mediapark.rocketball.utils.Converter
import lt.mediapark.rocketball.utils.DistanceCalc
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap

@Transactional
class EventService {

  Event saveEvent(GrailsParameterMap params) {
    if (params.id) {
      Event.get(Converter.coerceToLong(params.id))?.with {
        eventName = params.eventName
        eventStart = params.eventStart
        eventEnd = params.eventEnd
        eventRadius = Converter.coerceToLong(params.eventRadius)
        try {
          eventLat = Double.parseDouble(params.eventLat)
          eventLng = Double.parseDouble(params.eventLng)
        } catch (Exception e) {
          log.error(e)
        }
        it.save(flush: true)
      }
    } else {
      new Event(params).save(flush: true)
    }
  }

  Event getClosestEvent(User user) {
    //check for user location in an event type place
    def possibleEvents = Event.findAllByEventStartLessThanEqualsAndEventEndGreaterThanEquals(new Date(), new Date())
    def firstEvent = possibleEvents.find {
      def distance = DistanceCalc.getHavershineDistance(it.eventLat, it.eventLng, user?.currLat, user?.currLng)
      distance < it.eventRadius
    }

    firstEvent
  }

  Event deleteEvent(def id) {
    def event = Event.get(Converter.coerceToLong(id))
    if (event) {
      event.delete(flush: true)
    }
    event
  }
}
