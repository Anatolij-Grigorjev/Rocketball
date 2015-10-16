package lt.mediapark.rocketball

import grails.converters.JSON
import lt.mediapark.rocketball.utils.Constants

class UsersController {

    def userService
    def converterService

    static allowedMethods = [
            index         : 'GET',
            list          : 'GET',
            logout        : 'GET',
            update        : 'POST',
            updatePassword: 'POST'
    ]

    def index = {

        def user = userService.get(params.id, true)
        if (!user) {
            render(status: 404, text: "User at id ${params.id} doesn`t exist.")
        } else {
            def requestorUser = null
            if (params.requestor) {
                Long requestorId = Long.parseLong(params.requestor)
                if (requestorId == user.id) {
                    requestorUser = user
                } else {
                    requestorUser = userService.get(requestorId, false)
                }
            }
            def map = converterService.userToJSON(user, requestorUser)

            render map as JSON
        }
    }


    def list = {
        String type = params.type
        Long userId = Long.parseLong(params.id)
        def list = userService."${type}List"(userId)
        if (list == null) {
            return render(status: 404, text: "User at id ${params.id} doesn`t exist.")
        }

        render list as JSON

    }


    def coords = {
        def user = userService.get(params.id, false)
        if (!user) {
            return render(status: 404, text: "User at id ${params.id} doesn`t exist.")
        }
        //no point in updating too fast
        Long lastUpdate = userService.loggedInUsers[(user.id)]
        if ((lastUpdate != null) && (new Date().time - lastUpdate) > Constants.HEARTBEAT_PERIOD_MS) {
            userService.updateCoords(user, (Map) request.JSON)
        }

        render 200
    }

    def update = {
        def user = userService.get(params.id, false)
        if (!user) {
            return render(status: 404, text: "User at id ${params.id} doesn`t exist.")
        }
        user = userService.updateUser(user, (Map) request.JSON)
        def map = converterService.userToJSON(user)

        render map as JSON
    }

    def updatePassword = {
        def user = userService.get(params.id, false)
        if (!user) {
            return render(status: 404, text: "User at id ${params.id} doesn`t exist.")
        }
        def passSha1 = request.JSON.password
        def oldPassSha1 = request.JSON.oldPassword
        boolean changed = userService.updateUserPassword(user, oldPassSha1, passSha1, false)
        if (changed) {
            user.save(flush: true)
            def map = converterService.userToJSON(user)

            return render(map) as JSON
        } else {
            return render(status: 403, text: "Old password incorrect!")
        }
    }

    def logout = {
        def user = userService.get(params.id, false)
        if (user) {
            log.info("Logging out user ${user.id}!")
            userService.loggedInUsers.remove(user.id)
            userService.clearCoords(user)
            return render(status: 200)
        } else {
            return render(status: 400, text: "User not logged in or non-existant!")
        }
    }

}
