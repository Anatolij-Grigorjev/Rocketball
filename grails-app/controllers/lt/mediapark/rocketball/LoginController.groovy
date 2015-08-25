package lt.mediapark.rocketball

import grails.converters.JSON
import org.springframework.web.multipart.commons.CommonsMultipartFile

class LoginController {

    static allowedMethods = [
            regPicture : 'POST',
            regText    : 'POST',
            loginFB    : 'GET',
            loginNative: 'POST',
            regNative  : 'POST',
            loginNative: 'POST'
    ]

    def userService
    def converterService

    final int MIN_SALT_LENGTH = 40

    def regPicture = {
        CommonsMultipartFile picture = request.getFile('photo')
        def pic = new Picture(name: picture.name, data: picture.bytes)
        def tempUsers = userService.tempUsers
        User user
        def fbId = params.id
        if (tempUsers[fbId]) {
            user = tempUsers[fbId]
        } else {
            user = new User(userFbId: Long.parseLong(fbId))
            tempUsers[fbId] = user
        }
        user.picture = pic

        render(status: 200)
    }

    def regText = {
        def fbId = params.id
        def user = userService.tempUsers[(fbId)]
        if (user) {
            log.info "Found temp user for facebook id ${fbId}"
            user.name = request.JSON.name
            user.description = request.JSON.description
            user = userService.finalizeUser(user)
            userService.loggedInUsers << [(user.id): new Date().time]

            def userMap = converterService.userToJSON(user)
            userService.tempUsers.remove(fbId)

            render userMap as JSON

        } else {
            def errorMap = [status: 204, message: "No user with id ${params.id} found in those waiting to get registered!"]
            render errorMap as JSON
        }
    }

    def loginFB = {
        def fbId = Long.parseLong(params.id)
        def user = userService.userByFbId(fbId)
        if (user) {
            userService.loggedInUsers << [(user.id): new Date().time]
            def map = converterService.userToJSON(user)
            render map as JSON
        } else {
            def errorMap = ['registered': false]
            render errorMap as JSON
        }
    }

    def loginNative = {

        String email = request.JSON.email
        String password = request.JSON.password
        if (email && password) {
            def user = User.findByEmail(email)
            if (!user) {
                return render(403)
            }
            def hash = (password + user.salt).encodeAsSHA256()
            if (hash == user.passwordHash) {
                def map = converterService.userToJSON(user)
                render map as JSON
            } else {
                return render(403)
            }
        } else {
            return render(403)
        }
    }


    def regNative = {
        String email = request.JSON.email
        String password = request.JSON.password

        if (email && password && !User.all.email.any { it.equals(email) }) {

            def salt = userService.generateSalt(password?.length() > MIN_SALT_LENGTH ? password.length() : MIN_SALT_LENGTH)
            def actualPass = (password + salt).encodeAsSHA256()

            User user = new User(email: email, passwordHash: actualPass, salt: salt)
            def uuid = UUID.randomUUID().toString()
            userService.tempUsers[(uuid)] = user
            def map = [uuid: uuid]
            render map as JSON

        } else {
            render 401
        }
    }

}
