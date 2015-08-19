package lt.mediapark.rocketball

class Picture {

    static constraints = {
        data nullable: false, maxSize: 3980 * 2680
    }

    String name
    byte[] data
    PhotoAlbum album
}
