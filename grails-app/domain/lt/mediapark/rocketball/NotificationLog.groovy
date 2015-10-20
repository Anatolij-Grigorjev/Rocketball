package lt.mediapark.rocketball

import lt.mediapark.rocketball.message.ChatMessage

class NotificationLog {

    static constraints = {
        notified nullable: false
        notification nullable: false
    }

    User notified
    ChatMessage notification
}
