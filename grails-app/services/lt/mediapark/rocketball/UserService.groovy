package lt.mediapark.rocketball

import grails.transaction.Transactional
import groovyx.gpars.GParsPool
import lt.mediapark.rocketball.utils.Constants
import lt.mediapark.rocketball.utils.Converter
import lt.mediapark.rocketball.utils.DistanceCalc

import java.security.SecureRandom

@Transactional
class UserService {

    final int MIN_SALT_LENGTH = 40

    def converterService
    SecureRandom secureRandom = new SecureRandom("This is a seed".bytes)

    Map<String, User> tempUsers = [:]
    Map<Long, Long> loggedInUsers = Collections.synchronizedMap([:])

    def finalizeUser(User user) {
        if (user.picture) user.picture.save()
        user = user.save(flush: true)
        log.info("User ${user.name} added!")
        user
    }

    def userByFbId(long fbId) {
        User.findByUserFbId(fbId)
    }


    def User get(def anyUserId, boolean canBeOffline = false) {
        Long userId = Converter.coerceToLong(anyUserId)
        log.info("User id ${userId} was requested,${canBeOffline ? " " : " NOT "}OK to search storage users...")
        def user
        if (loggedInUsers.containsKey(userId)) {
            log.info("Fetching ${userId} from cache!")
            user = User.get(userId)
        }
        if (!userId && canBeOffline) {
            log.info("Fetching ${userId} from storage!")
            user = User.findById(userId)
        }
        user
    }

    def favoritesList(Long userId) {

        def user = get(userId, false)
        def mapList
        GParsPool.withPool {
            def userFavsList = user.favorites.findAllParallel { User fav -> !fav.blocked.contains(user) } as List<User>
            mapList = userFavsList.collectParallel { converterService.userToJSON(it, user) } as List<Map>
            mapList.sort(true) { a, b -> a.distance <=> b.distance }
        }
        mapList
    }

    def closeList(Long userId) {

        def user = get(userId, false)
        List<User> users = User.createCriteria().list {
            not {
                'in'('id', [userId])
            }
        } as List<User>

        def mapList
        GParsPool.withPool {
            def filtered = users.findAllParallel {
                (DistanceCalc.getHavershineDistance(it, user) <= Constants.PEOPLE_RADIUS_M
                        && !it.blocked.contains(user))
            }
            mapList = filtered.collectParallel { converterService.userToJSON(it, user) } as List<Map>
            mapList.sort(true) { a, b -> a.distance <=> b.distance }
        }
        mapList
    }

    def updateCoords(User user, Map coords) {
        if (coords?.lat) user?.currLat = coords.lat
        if (coords?.lng) user?.currLng = coords.lng
        loggedInUsers[(user.id)] = new Date().time
        if (!user.userFbId || user.userFbId > 0)
            log.debug("Updated user coords! User ${user.name} is now at (${coords.lat};${coords.lng})")
        user.save(flush: true)
    }

    User updateUser(User user, Map updates) {
        if (updates?.name) user?.name = updates.name
        if (updates?.description) user?.description = updates.description
        if (updates?.picId) user?.picture = Picture.get(Converter.coerceToLong(updates.picId))
        ['favorites', 'blocked'].each { word ->
            if (updates?."${word}") {
                updates."${word}".each {
                    def id = Converter.coerceToLong(it.key)
                    def shouldStuff = Boolean.parseBoolean(it.value)
                    if (shouldStuff) {
                        user?."${word}" << User.get(id)
                    } else {
                        user?."${word}"?.remove(User.get(id))
                    }
                }
            }
        }

        user.save(flush: true)
    }

    /**
     * Set user password and salt to new values based on password hash or whatever it is
     * @param user user to set password for
     * @param password password SHA1 hash
     * @return
     */
    def updateUserPassword(User user, String oldPassword, String password, boolean isTemp = false) {
        def actualOldPass = (oldPassword + user.salt).encodeAsSHA256()
        if (actualOldPass == user.passwordHash) {
            updateUserPassword(user, password, isTemp)
            true
        } else {
            false
        }
    }

    def updateUserPassword(User user, String password, boolean isTemp) {
        def salt = generateSalt(password?.length() > MIN_SALT_LENGTH ? password.length() : MIN_SALT_LENGTH)
        def actualPass = (password + salt).encodeAsSHA256()
        user.passwordHash = actualPass
        user.salt = salt
        user.tempPassword = isTemp
    }

    def clearCoords(def userId) {
        def user = get(userId)
        if (!user.userFbId || user.userFbId > 0) {
            log.debug('Removing coords for user: ' + user.name)
            user.currLat = null
            user.currLng = null
            user.save(flush: true)
        }
    }

    def generateSalt(int length) {

        byte[] saltBytes = new byte[length]
        secureRandom.nextBytes(saltBytes)

        new String(saltBytes)
    }

    def generateTempPass(User user) {
        def newPass = ''
        //1 to 3 words
        int wordsTotal = Constants.randomWords.length
        (secureRandom.nextInt(3) + 1).times {
            newPass += Constants.randomWords[secureRandom.nextInt(wordsTotal)]
                    ."${newPass ? 'capitalize' : 'toLowerCase'}"()
        }
        newPass += ('' + secureRandom.nextInt(100)).padLeft(2, '0')
        updateUserPassword(user, newPass.encodeAsSHA1(), true)
        user.save(flush: true)
        newPass
    }
}
