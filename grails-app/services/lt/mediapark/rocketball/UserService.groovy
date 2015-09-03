package lt.mediapark.rocketball

import grails.transaction.Transactional
import groovyx.gpars.GParsPool
import lt.mediapark.rocketball.utils.Constants
import lt.mediapark.rocketball.utils.Converter
import lt.mediapark.rocketball.utils.DistanceCalc

import java.security.SecureRandom

@Transactional
class UserService {

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
            mapList = user.favorites.findAllParallel { User fav -> !fav.blocked.contains(user)
            }.collectParallel {
                converterService.userToJSON(it, user)
            }
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
            mapList = filtered.collectParallel { converterService.userToJSON(it, user) }
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
}
