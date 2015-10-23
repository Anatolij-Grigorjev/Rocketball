package lt.mediapark.rocketball

import grails.converters.JSON
import lt.mediapark.rocketball.utils.Constants
import org.springframework.web.multipart.commons.CommonsMultipartFile

class MediaController {

    def userService
    def mediaService


    def index = {
        def picture = Picture.get(Long.parseLong(params.id))
        if (picture) {
            response.contentType = 'image/jpeg'
            response.setHeader('Content-disposition', "attachment;filename=${picture.id}.jpg")
            response.contentLength = picture.data.length

            response.outputStream << new ByteArrayInputStream(picture.data)
            response.outputStream.flush()

            return null
        } else {
            render(status: 404)
        }
    }

    def upload = {
        Long userId = Long.parseLong(params.id)
        def user = userService.get(userId)
        //cant upload pic into the ether, so only if valid userId
        if (user) {
            CommonsMultipartFile image = request.getFile(Constants.AVATAR_PICTURE_FILE_KEY)
            def picture = mediaService.saveAsAvatar(image, user)

            def picIdMap = [pictureId: picture.id]

            render picIdMap as JSON
        } else {
            return render([registered: false] as JSON)
        }
    }


    def getVideo = {
        Long requestorId = Long.parseLong(params.requestor)
        if (!userService.get(requestorId)) {
            return render([registered: false] as JSON)
        }
        Long vidId = Long.parseLong(params.id)

        def video = mediaService.getVideoFromMessage(vidId, requestorId)
        if (!video) {
            return render(status: 404, message: 'Video at id not found or user has no rights to see it')
        }

        response.contentType = 'video/mp4'
        response.setHeader('Content-disposition', "attachment;filename=${video.id}.mp4")
        response.contentLength = video.data.length

        response.outputStream << new ByteArrayInputStream(video.data)
        response.outputStream.flush()

        return null
    }

    def getPhotoAlbum = {
        Long requestorId = params.requestor ? Long.parseLong(params.requestor) : null
        if (!userService.get(requestorId)) {
            return render([registered: false] as JSON)
        }
        Long albumId = params.id ? Long.parseLong(params.id) : null

        ByteArrayOutputStream outputBytes = mediaService.createAlbumZip(albumId, requestorId)

        if (!outputBytes) {
            return render(status: 404, message: 'Album at id not found or user has no rights to see it')
        }

        response.setHeader("Content-disposition", "filename=\"album-${albumId}.zip\"")
        response.contentType = "application/zip"
        response.contentLength = outputBytes.size()

        response.outputStream << outputBytes.toByteArray()
        response.outputStream.flush()

        return null
    }


}
