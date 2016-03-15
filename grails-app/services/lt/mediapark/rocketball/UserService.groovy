package lt.mediapark.rocketball

import grails.transaction.Transactional
import lt.mediapark.rocketball.utils.Constants
import lt.mediapark.rocketball.utils.Converter
import lt.mediapark.rocketball.utils.DistanceCalc

import java.security.SecureRandom

@Transactional
class UserService {

  final int MIN_SALT_LENGTH = 40

  def converterService
  def eventService
  def chatService
  SecureRandom secureRandom = new SecureRandom("This is a seed".bytes)

  Map<String, User> tempUsers = [:]
  static Map<Long, Long> loggedInUsers = Collections.synchronizedMap([:])

  def finalizeUser(User user) {
    if (user.picture) user.picture.save()
    user.isOnline = true
    user = user.save(flush: true)
    log.info("User ${user.name} added!")
    user
  }

  def userByFbId(long fbId) {
    User.findByUserFbId(fbId)
  }


  def User get(def anyUserId, boolean canBeOffline = false) {
    Long userId = Converter.coerceToLong(anyUserId)
    log.debug("User id ${userId} was requested,${canBeOffline ? " " : " NOT "}OK to search storage users...")
    def user = null
    if (loggedInUsers.containsKey(userId)) {
      log.debug("Fetching ${userId} from cache!")
      user = User.get(userId)
    }
    if (!user && canBeOffline) {
      log.debug("Fetching ${userId} from storage!")
      user = User.findById(userId)
    }
    user
  }

  def favoritesList(Long userId) {

    def user = get(userId, false)
    if (!user) {
      return null
    }
    def mapList
    List<User> userFavsList = user.favorites.findAll { !it.blocked.contains(user) } as List<User>
    mapList = userFavsList.collect { converterService.userToJSON(it, user) } as List<Map>
    mapList.sort(true) { a, b -> a.distance <=> b.distance }
    mapList
  }

  def closeList(Long userId) {

    def user = get(userId, false)
    if (!user) {
      return null
    }
    List<User> users = User.createCriteria().list {
      not {
        'in'('id', [userId])
      }
    } as List<User>

    def mapList
    List<User> filtered = []
    filtered = users.findAll {
      (DistanceCalc.getHavershineDistance(it, user) <= Constants.PEOPLE_RADIUS_M
        && !it.blocked?.contains(user))
    }
    mapList = filtered?.collect { converterService.userToJSON(it, user) } as List<Map>
    mapList.sort(true) { a, b -> a.distance <=> b.distance }
    def event = eventService.getClosestEvent(user.currLat, user.currLng)
    [users: mapList, event: event?.eventName]
  }


  def updateCoords(User user, Map coords) {
    if (coords?.lat && coords?.lng) {
      def distance = Double.POSITIVE_INFINITY
      if (user.hasLocation()) {
        distance = DistanceCalc.getHavershineDistance(user.currLat, user.currLng, coords.lat, coords.lng)
      }
      if (distance > Constants.MIN_WALK_DISTANCE) {
        user.currLng = coords.lng
        user.currLat = coords.lat
        loggedInUsers[(user.id)] = new Date().time
        if (!user.userFbId || user.userFbId > 0)
          log.debug("Updated user coords! User ${user.name} is now at (${coords.lat};${coords.lng})")
        user.save(flush: true)
      }
    }
  }

  User updateUser(User user, Map updates) {
    if (updates?.name) user?.name = updates.name
    if (updates?.description) user?.description = updates.description
    if (updates?.deviceToken || updates?.registrationId) {
      if (updates?.deviceToken != null) user?.deviceToken = updates.deviceToken
      if (updates?.registrationId != null) user?.registrationId = updates.registrationId
      chatService.tryFlushMessagesAsync(user)
    }
    if (updates?.email) user?.email = updates.email
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
    user.save()
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
    clearCoords(user)
  }

  boolean clearCoords(User user) {
    log.debug('Removing coords for user: ' + user.name)
    if (user.hasLocation()) {
      user.currLat = null
      user.currLng = null
      user.save()
      return true
    }
    false
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
    user.save()
    newPass
  }
}
