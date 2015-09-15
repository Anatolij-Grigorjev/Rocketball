package lt.mediapark.rocketball.message

import lt.mediapark.rocketball.PhotoAlbum

class PhotoMessage extends ChatMessage<PhotoAlbum> {

    static constraints = {
        photoAlbum nullable: false
    }

    static fetchMode = [
            photoAlbum: 'eager'
    ]

    PhotoAlbum photoAlbum

    @Override
    PhotoAlbum getContent() {
        return photoAlbum
    }
}
