package lt.mediapark.rocketball

import grails.converters.JSON

class UsersController {

    def userService
    def converterService

    static allowedMethods = [
            index : 'GET',
            list  : 'GET',
            update: 'POST'
    ]

    def index = {

        def user = userService.get(Long.parseLong(params.id), true)
        if (!user) {
            render 404
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

        render list as JSON

    }


    def coords = {
        def user = userService.get(Long.parseLong(params.id), false)
        userService.updateCoords(user, request.JSON)

        render 200
    }

    def update = {
        def user = userService.get(Long.parseLong(params.id), false)
        user = userService.updateUser(user, (Map) request.JSON)

        def map = converterService.userToJSON(user)

        render map as JSON
    }

}
