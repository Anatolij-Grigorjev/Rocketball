package lt.mediapark.rocketball

class Event {

  String eventName
  Double eventLat
  Double eventLng
  Date eventStart
  Date eventEnd
  Long eventRadius = 1

  static constraints = {
    eventName nullable: false
    eventStart validator: { it.before(eventEnd) }
    eventEnd validator: { it.after(eventStart) }
  }
}
