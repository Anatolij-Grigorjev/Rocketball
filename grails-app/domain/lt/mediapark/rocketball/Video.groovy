package lt.mediapark.rocketball

class Video {

    static constraints = {
        data nullable: false, maxSize: Integer.MAX_VALUE
    }

    String name
    byte[] data
}
