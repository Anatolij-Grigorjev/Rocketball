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

    public PhotoAlbum leftShift(Picture photo) {
        if (!this.photos) {
            this.photos = [] as Set
        }
        this.photos << photo
        this
    }

}
