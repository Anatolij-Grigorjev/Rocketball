package lt.mediapark.rocketball

class User {

    static constraints = {
        name nullable: false
        userFbId nullable: true, unique: true
        description maxSize: 5000
        currLat nullable: true
        currLng nullable: true
        email nullable: true, unique: true
        passwordHash nullable: true
        salt nullable: true
        tempPassword nullable: false
    }

    static hasMany = [
            'favorites': User,
            'blocked'  : User
    ]

    String name
    String email
    String passwordHash
    String salt
    Picture picture
    String description
    Long userFbId
    Double currLat
    Double currLng
    String deviceToken
    String registrationId
    Boolean tempPassword = Boolean.FALSE

    boolean hasLocation() {
        this.currLat && this.currLng
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof User)) return false

        User user = (User) o

        if (id != user.id) return false
        if (name != user.name) return false
        if (userFbId != user.userFbId) return false

        return true
    }

    int hashCode() {
        int result
        result = (name != null ? name.hashCode() : 0)
        result = 31 * result + (userFbId != null ? userFbId.hashCode() : 0)
        result = 31 * result + (id != null ? id.hashCode() : 0)
        return result
    }

    boolean isBlockedBy(User other) {
        return other?.blocked?.contains(this)
    }
}
