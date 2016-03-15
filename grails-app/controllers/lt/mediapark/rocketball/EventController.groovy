package lt.mediapark.rocketball

import lt.mediapark.rocketball.utils.Converter

import javax.servlet.http.Cookie

class EventController {

  def eventService

  static String currentToken

  static allowedMethods = [
    auth: 'GET'
  ]
  public static final String ROCKETBALL_AUTH = 'X-Rocketball-Auth'

  def auth() {
    def id = Converter.coerceToLong params.id
    if (id) {
      def user = User.get(id)
      if (user?.isAdmin) {
        render(view: 'inputAuth', model: [name: user.name])
      } else {
        render 403
      }
    } else {
      render 400
    }
  }

  def saveEvent() {
    def token = request.cookies.find { it.name == ROCKETBALL_AUTH }?.value
    if (token?.equals(currentToken)) {
      def event = eventService.saveEvent(params)
      redirect(controller: 'event', action: 'makeForm', params: [eventName: event?.eventName, eventId: event?.id])
    } else {
      render 403
    }
  }

  def makeForm() {
    if (!params.eventName) {
      String email = params.email
      String password = params.password
      if (email && password) {
        def user = User.findByEmail(email)
        if (!user || !user.isAdmin) {
          return render(status: 403)
        }
        def hash = (password + user.salt).encodeAsSHA256()
        if (hash == user.passwordHash) {
          currentToken = UUID.randomUUID().toString()
          response.addCookie(new Cookie(ROCKETBALL_AUTH, currentToken))
        } else {
          return render(status: 401)
        }
      } else {
        return render(status: 400)
      }
    }
    render(view: 'eventData', model: [eventName: params.eventName, eventId: params.eventId])
  }
}
