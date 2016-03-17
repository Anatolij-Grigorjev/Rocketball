package lt.mediapark.rocketball

import grails.util.Environment
import lt.mediapark.rocketball.utils.Converter

import javax.servlet.http.Cookie

class EventController {

  def eventService

  static String currentToken

  static allowedMethods = [
    auth       : 'GET',
    performAuth: 'POST'
  ]
  public static final String ROCKETBALL_AUTH = 'X-Rocketball-Auth'

  def auth() {
    def id = Converter.coerceToLong params.id
    if (id) {
      def user = User.get(id)
      if (user?.isAdmin) {
        render(view: 'inputAuth', model: [name: user.name])
      } else {
        render(status: 403)
      }
    } else {
      render(status: 400)
    }
  }

  def saveEvent() {
    def token = request.cookies.find { it.name == ROCKETBALL_AUTH }?.value
    if (token?.equals(currentToken)) {
      def event = eventService.saveEvent(params)
      redirect(controller: 'event', action: 'list', params: [eventAction: 'save'
                                                             , eventName: event?.eventName
                                                             , eventId  : event?.id]
      )
    } else {
      render(status: 403)
    }
  }

  def list() {

    def token = request.cookies.find { it.name == ROCKETBALL_AUTH }?.value
    if (token?.equals(currentToken)) {
      render(view: 'eventList', model: [events       : Event.all
                                        , eventName  : params.eventName
                                        , eventAction: params.eventAction
                                        , eventId    : params.eventId]
      )
    } else {
      render(status: 403)
    }
  }

  def performAuth() {
    String email = params.email
    String password = !(Environment.current == Environment.PRODUCTION) ? params.password : params.password?.encodeAsSHA1()
    if (email && password) {
      def user = User.findByEmail(email)
      if (!user || !user.isAdmin) {
        return render(status: 403)
      }
      def hash = (password + user.salt).encodeAsSHA256()
      if (hash == user.passwordHash) {
        currentToken = UUID.randomUUID().toString()
        def cookie = new Cookie(ROCKETBALL_AUTH, currentToken)
        response.addCookie(cookie)
        redirect(controller: 'event', action: 'list')
      } else {
        return render(status: 401)
      }
    } else {
      return render(status: 400)
    }
  }

  def delete() {
    def token = request.cookies.find { it.name == ROCKETBALL_AUTH }?.value
    if (token?.equals(currentToken) && params.id) {
      def event = eventService.deleteEvent(params.id)
      redirect(controller: 'event', action: 'list', params: [eventAction: 'del'
                                                             , eventName: event?.eventName
                                                             , eventId  : event?.id]
      )
    } else {
      render(status: 403)
    }
  }

  def makeForm() {
    def token = request.cookies.find { it.name == ROCKETBALL_AUTH }?.value
    if (token?.equals(currentToken)) {
      def model = [:]
      if (params.id) {
        model << [id: params.id]
        model.putAll(Event.get(Converter.coerceToLong(params.id))?.properties)
      }
      render(view: 'eventData', model: model)
    } else {
      render(status: 403)
    }
  }
}
