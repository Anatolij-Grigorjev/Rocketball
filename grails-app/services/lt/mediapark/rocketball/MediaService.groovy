package lt.mediapark.rocketball

import grails.transaction.Transactional
import lt.mediapark.rocketball.message.PhotoMessage
import lt.mediapark.rocketball.message.VideoMessage
import org.springframework.web.multipart.commons.CommonsMultipartFile

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

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

    Video getVideoFromMessage(Long vidId, Long userId) {
        if (vidId && userId) {
            def vidMessage = VideoMessage.findByVideo(Video.get(vidId))

            //only the privileged can access the video
            //meaning only with right id, but also only people privvy to the conversation
            if (vidMessage.sender.id == userId
                    || vidMessage.receiver.id == userId) {

                return vidMessage.video
            }
        }
        null
    }

    ByteArrayOutputStream createAlbumZip(Long albumId, Long requestorId) {
        if (albumId && requestorId) {

            def albumMessage = PhotoMessage.findByPhotoAlbum(PhotoAlbum.get(albumId))

            //only people allowed to look at album are those who participated in conversation
            if (albumMessage.sender.id == requestorId
                    || albumMessage.receiver.id == requestorId) {

                ByteArrayOutputStream bytesStream = new ByteArrayOutputStream()
                ZipOutputStream zippedBytes = new ZipOutputStream(bytesStream)

                //add all photos to the ZIP
                albumMessage.photoAlbum.photos.each { photo ->
                    zippedBytes.putNextEntry(new ZipEntry("${photo.name}-${photo.id}.jpg"))
                    zippedBytes << photo.data
                    zippedBytes.closeEntry()
                }
                zippedBytes.finish()

                return bytesStream
            }
        }
        null
    }
}
