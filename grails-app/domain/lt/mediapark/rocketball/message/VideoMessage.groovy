package lt.mediapark.rocketball.message

import lt.mediapark.rocketball.Video

class VideoMessage extends ChatMessage<Video> {

    static constraints = {
        video nullable: false
    }

    Video video

    @Override
    Video getContent() {
        return video
    }
}
