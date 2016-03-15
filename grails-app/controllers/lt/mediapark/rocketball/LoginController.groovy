package lt.mediapark.rocketball

import grails.converters.JSON
import grails.util.Environment
import org.springframework.web.multipart.commons.CommonsMultipartFile

class LoginController {

    static allowedMethods = [
            regPicture : 'POST',
            regText    : 'POST',
            loginFB    : 'GET',
            loginNative: 'POST',
            regNative  : 'POST',
            loginNative: 'POST',
            resetPwd   : 'POST'
    ]

    def userService
    def converterService
    def mailService
    def chatService

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
            user.deviceToken = request.JSON.deviceToken
            user.registrationId = request.JSON.registrationId

            user = userService.finalizeUser(user)
            userService.loggedInUsers << [(user.id): 0]

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
            user.isOnline = true
            user.save()
            userService.loggedInUsers << [(user.id): 0]
            chatService.tryFlushMessagesAsync(user)
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
                return render(status: 403)
            }
            def hash = (password + user.salt).encodeAsSHA256()
            if (hash == user.passwordHash) {
                user.isOnline = true
                user.save()
                userService.loggedInUsers << [(user.id): 0]
                chatService.tryFlushMessagesAsync(user)
                def map = converterService.userToJSON(user)
                render map as JSON
            } else {
                return render(status: 401)
            }
        } else {
            return render(status: 400)
        }
    }


    def regNative = {
        String email = request.JSON.email
        String password = request.JSON.password

        if (email && password && !User.all.email.any { it.equals(email) }) {

            User user = new User(email: email, isAdmin: !(Environment.current == Environment.PRODUCTION))
            userService.updateUserPassword(user, password, false)

            def uuid = UUID.randomUUID().toString()
            userService.tempUsers[(uuid)] = user
            def map = [uuid: uuid]
            render map as JSON

        } else {
            render(status: 400)
        }
    }

    def resetPwd = {
        String email = request.JSON.email
        User user = User.findByEmail(email)
        //gotta make sure its a valid email in system
        if (email && user) {
            try {
                mailService.sendMail {
                    to "${user.name}<${email}>"
                    from "Rocketball Support<support@rocketballapp.com>"
                    subject 'Rocketball password reset'
                    body(view: "/mail/resetPasswordEmail",
                            model: [user: user, tempPass: userService.generateTempPass(user)])
                }
            } catch (Exception ex) {
                log.error("Problem sending mail: ${ex.message} to ${email} for ${user}", ex)
                render(status: 512, "Email server down")
            }
            render(status: 204)
        } else {
            render(status: 400)
        }
    }

}
