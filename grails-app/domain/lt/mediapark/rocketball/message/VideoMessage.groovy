package lt.mediapark.rocketball.message

import lt.mediapark.rocketball.Video

class VideoMessage extends ChatMessage<Video> {

    static constraints = {
        video nullable: false
    }

    static fetchMode = [
            video: 'eager'
    ]

    Video video

    @Override
    Video getContent() {
        return video
    }
}
