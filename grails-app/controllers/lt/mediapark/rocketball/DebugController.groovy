package lt.mediapark.rocketball

import com.google.android.gcm.server.Message
import com.relayrides.pushy.apns.util.ApnsPayloadBuilder
import grails.converters.JSON
import lt.mediapark.rocketball.message.ChatMessage

class DebugController {

    private static final List<String> names = ['Elvie Flett'
                                               , 'Beatriz Hardwick'
                                               , 'Ethan Brigman'
                                               , 'Jaunita Bluford'
                                               , 'Fabiola Predmore'
                                               , 'Jeana Glover'
                                               , 'Nona Pea'
                                               , 'Connie Ruffner'
                                               , 'Philip Reetz'
                                               , 'Monet Marlin'
                                               , 'Leslie Rodkey'
                                               , 'Signe Bresnahan'
                                               , 'Vinnie Siers'
                                               , 'Bobbi Fleetwood'
                                               , 'Dovie Lamarca'
                                               , 'Jannette Alegria'
                                               , 'Rutha Post'
                                               , 'Wilson Coghill'
                                               , 'Georgeann Marez'
                                               , 'Bethanie Brownfield'
                                               , 'Kaleigh Smith'
                                               , 'Cyndi Pressnell'
                                               , 'Pamala Pilon'
                                               , 'Flossie Mccardell'
                                               , 'Jalisa Trees'
                                               , 'Dominque Reineke'
                                               , 'Ammie Schroyer'
                                               , 'Soila Goranson'
                                               , 'Roscoe Westrich'
                                               , 'Cathleen Polasek'
                                               , 'Jules Valerio'
                                               , 'Suzanna Sergi'
                                               , 'Dorine Lopiccolo'
                                               , 'Tonita Hillock'
                                               , 'Willia Bowes'
                                               , 'Lelia Beall'
                                               , 'Aurelio Freese'
                                               , 'Brice Kreitzer'
                                               , 'Criselda Kovach'
                                               , 'Eun Parry'
                                               , 'Rudy Paavola'
                                               , 'Madalyn Blas'
                                               , 'Lena Tsosie'
                                               , 'Alla Stratford'
                                               , 'Sheryl Tsui'
                                               , 'Carmen Flicker'
                                               , 'Jolanda Yip'
                                               , 'Debrah Verrill'
                                               , 'Farrah Sherrod'
                                               , 'Sallie Bridwell'
    ]


    def userService
    def converterService

    def login = {
//        if (Environment.DEVELOPMENT.equals(Environment.current)) {
        int amount = Integer.parseInt(params.id)
            def users = User.all
            def rnd = new Random()
            def result = []
//            if (Environment.PRODUCTION.equals(Environment.current)) amount = 0
            amount.times {
                //54.689566, 25.272500
                Double latOrigin = params.lat ? Double.parseDouble(params.lat) : 54.689566
                Double lngOrigin = params.lng ? Double.parseDouble(params.lng) : 25.272500
                def user
                if (users.size() > it) {
                    //login some existing users
                    user = users[it]
                } else {
                    //need more users
                    user = new User()
                    //take random first name and random last name
                    user.name = names[rnd.nextInt(names.size())].split('\\s+')[0] + ' ' + names[rnd.nextInt(names.size())].split('\\s+')[1]
                    user.description = "This describes me best ${def desc = ""; rnd.nextInt(7).times { desc += (rnd.nextDouble() + ' ') }; desc}"
                    user.userFbId = -1 * Math.abs(rnd.nextLong())
                    File image = downloadImage('http://lorempixel.com/320/320/')
                    if (image) {
                        Picture picture = new Picture(data: image.bytes, name: image.name)
                        picture = picture.save()
                        user.picture = picture
                    }
                }
                user.currLat = latOrigin - (rnd.nextDouble() / rnd.nextInt(10000))
                user.currLng = lngOrigin + (rnd.nextDouble() / rnd.nextInt(10000))
                user.save()
                result << user
                userService.loggedInUsers << [(user.id): new Date().time]
            }
            def json = result.collect { converterService.userToJSON(it) }
            render json as JSON
        }
//        else {
//            render(status: 200)
//        }
//    }

    def remove = {
        //test users all have this feature
        def debugUsers = User.findAllByUserFbIdIsNotNullAndUserFbIdLessThan(0L)
        debugUsers.each { user ->
            ChatMessage.executeUpdate("delete ChatMessage cm where cm.sender = :user OR cm.receiver = :user", [user: user])
            User.all.each { innerUser ->
                innerUser.blocked.remove(user)
                innerUser.favorites.remove(user)
            }
            User.saveAll(User.all)
        }
        debugUsers.each { it.delete(flush: true) }
        render(status: 200, text: "Deleted ${debugUsers.size()} users.")
    }

    def push = {
        def token = params.id
        def rnd = new Random()
        sendAPNSNotification(token) { ApnsPayloadBuilder builder ->
            //total message cannot exceed 250 bytes
            builder.with {
                alertBody = "This message is a test done at ${new Date()}"
                badgeNumber = rnd.nextInt(17)
                addCustomProperty('senderId', "<sender id Long>") //8 + 8 bytes
                addCustomProperty('senderName', "<sender name String>") //10 + ~15 bytes
                addCustomProperty('senderPicId', "<sender pic id Long") //11 + 8 bytes
            }
        }

        //sendAPNSAlt(token, "This is a test message");


        render(status: 200)
    }

    def gcm = {
        def regId = params.id
        sendGCMNotification(regId) { Message.Builder builder ->
            builder.with {
                collapseKey('testKey')
                timeToLive(60)
                delayWhileIdle(true)
                data = ['message'      : 'This be the test message text'
                        , 'senderId'   : '<extra param sender Id>'
                        , 'senderName' : '<extra param sender name>'
                        , 'senderPicId': '<extra param sender pic Id>'] as Map<String, String>
            }
        }

        render(status: 200)
    }


    def insys = {
        def results = [amount: userService.loggedInUsers.size(), users: userService.loggedInUsers]
        render results as JSON
    }


}
