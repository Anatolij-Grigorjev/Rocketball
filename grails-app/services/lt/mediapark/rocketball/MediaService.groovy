package lt.mediapark.rocketball

import grails.transaction.Transactional
import lt.mediapark.rocketball.message.PhotoMessage
import lt.mediapark.rocketball.message.VideoMessage
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import org.springframework.web.multipart.commons.CommonsMultipartFile

import java.text.SimpleDateFormat
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Transactional
class MediaService {

    static def dateBasedNameFormatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_")

    Picture saveAsAvatar(CommonsMultipartFile image, User uploader) {
        Picture picture = new Picture(name: image.name ?:
                (dateBasedNameFormatter.format(new Date()) + uploader.id + '.png')
                , data: image.bytes)
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

    Video saveAsMP4Video(CommonsMultipartFile file) {
        Video video = new Video(name: file.name)
        def inFile = File.createTempFile('video', FilenameUtils.getExtension(file.name))
        inFile.withOutputStream { IOUtils.write(file.bytes, it) }
        def fileTypeCheck = "file ${inFile.absolutePath}".execute()
        fileTypeCheck.waitFor()
        def output = IOUtils.toString(fileTypeCheck.in)
        log.info("file command output: ${output}")
        //check if movie format received was MOV
        if (output.contains('Apple QuickTime')) {
            log.info('Video format received was Apple Quicktiem, converting to MPEG!')
            def outFile = File.createTempFile('new_video', '.mp4')
            log.info("Command: ${"ffmpeg -i ${inFile.absolutePath} -q 0 -y ${outFile.absolutePath}"}")
            def conversion = "ffmpeg -i ${inFile.absolutePath} -q 0 -y ${outFile.absolutePath}".execute()
            conversion.waitFor()
//            output = IOUtils.toString(conversion.in)
//            log.info("Conversion process for file ${inFile.name}:\n${output}")
            video.data = outFile.bytes
            outFile.delete()
        } else {
            video.data = file.bytes
        }
        video.save()
        inFile.delete()
        return video
    }

    Video getVideoFromMessage(Long vidId, Long userId) {
        if (vidId && userId) {
            def vidMessage = VideoMessage.findByVideo(Video.get(vidId))

            //only the privileged can access the video
            //meaning only with right id, but also only people privy to the conversation
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
