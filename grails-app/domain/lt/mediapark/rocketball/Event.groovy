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
    eventStart nullable: false, validator: { date, instance -> date.before instance.eventEnd }
    eventEnd validator: { date, instance -> date.after instance.eventStart }
  }
}
