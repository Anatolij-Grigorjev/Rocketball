package lt.mediapark.rocketball

import grails.converters.JSON

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
        int amount = Integer.parseInt(params.id)
        def users = User.all.findAll { !userService.loggedInUsers.containsKey(it.id) }
        def rnd = new Random()
        def result = []
        amount.times {
            if (users.size() > it) {
                //login some existing users
                result << users[it]
                userService.loggedInUsers << [(users[it].id): new Date().time]
            } else {
                //need more users
                def user = new User()
                def latOrigin = 54.689566
                def lngOrigin = 25.272500
                user.currLat = latOrigin - (rnd.nextDouble() / rnd.nextInt(10000))
                user.currLng = lngOrigin + (rnd.nextDouble() / rnd.nextInt(10000))

                user.name = names[rnd.nextInt(names.size())]
                user.description = "This describes me best ${def desc = ""; rnd.nextInt(15).times { desc += (rnd.nextDouble() + ' ') }; desc}"
                user.userFbId = -1 * Math.abs(rnd.nextLong())
                File image = downloadImage('http://lorempixel.com/320/320/')
                if (image) {
                    Picture picture = new Picture(data: image.bytes, name: image.name)
                    picture = picture.save()
                    user.picture = picture
                }
                user.save()
                result << user
                userService.loggedInUsers << [(user.id): new Date().time]
            }
        }
        def json = result.collect { converterService.userToJSON(it) }
        render json as JSON
    }


}
