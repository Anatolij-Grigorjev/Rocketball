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
        }
    }


}
