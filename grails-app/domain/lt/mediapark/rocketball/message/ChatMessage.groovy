package lt.mediapark.rocketball.message

import lt.mediapark.rocketball.User

abstract class ChatMessage<T> {

    static constraints = {
        sender nullable: false
        receiver nullable: false
    }

    User sender
    User receiver

    Date sendDate
    Date receiveDate
    Date created = new Date()

    def isReceived() { !!receiveDate }

    abstract T getContent()

}
