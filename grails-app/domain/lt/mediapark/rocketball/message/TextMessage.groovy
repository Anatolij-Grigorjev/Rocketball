package lt.mediapark.rocketball.message

class TextMessage extends ChatMessage<String> {

    static constraints = {
        text nullable: false, maxSize: 65536
    }

    String text

    @Override
    String getContent() {
        return text
    }
}
