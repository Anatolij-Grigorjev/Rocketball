package lt.mediapark.rocketball

import grails.transaction.Transactional
import org.springframework.web.multipart.commons.CommonsMultipartFile

@Transactional
class MediaService {

    Picture saveAsAvatar(CommonsMultipartFile image, User uploader) {
        Picture picture = new Picture(name: image.name, data: image.bytes)
        picture = picture.save()
        uploader.picture = picture
        uploader.save(flush: true)
        picture
    }

    PhotoAlbum saveAsAlbum(Collection<CommonsMultipartFile> photos) {
        PhotoAlbum album = new PhotoAlbum()
        photos.each { photoFile ->
            def picture = new Picture(name: photoFile.name, data: photoFile.bytes, album: album)
            album << picture.save()
        }
        album.save()
    }

    Video saveAsVideo(CommonsMultipartFile file) {
        Video video = new Video(name: file.name, data: file.bytes)
        video.save()
    }
}
