package lt.mediapark.rocketball

class PhotoAlbum {

    static constraints = {
    }

    static mappedBy = [
            photos: 'album'
    ]

    static hasMany = [
            photos: Picture
    ]

    Set<Picture> photos = []
}
