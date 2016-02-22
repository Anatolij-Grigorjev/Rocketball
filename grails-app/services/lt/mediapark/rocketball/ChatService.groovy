package lt.mediapark.rocketball

import com.google.android.gcm.server.Message
import com.relayrides.pushy.apns.util.ApnsPayloadBuilder
import grails.transaction.Transactional
import lt.mediapark.rocketball.clsf.MessageType
import lt.mediapark.rocketball.message.ChatMessage
import lt.mediapark.rocketball.message.PhotoMessage
import lt.mediapark.rocketball.message.TextMessage
import lt.mediapark.rocketball.message.VideoMessage
import org.apache.commons.lang.WordUtils
import org.springframework.web.multipart.commons.CommonsMultipartFile

import java.util.concurrent.atomic.AtomicLong

@Transactional
class ChatService {

    def userService
    def mediaService

    public static final Integer MAX_MESSAGE_CHARS = 50
    private static AtomicLong androidMsgCollapseId = new AtomicLong(477);

    List<ChatMessage> getChatHistory(User requestor, User other, Date before, int limit) {
        def historyMessages = ChatMessage.createCriteria().list {
            or {
                and {
                    eq('sender.id', requestor.id)
                    eq('receiver.id', other.id)
                }
                and {
                    eq('sender.id', other.id)
                    eq('receiver.id', requestor.id)
                }
            }
            le('created', before)
            order('sendDate', 'desc')
            maxResults(limit)
        } as List<ChatMessage>
        //messages need to be resorted the other way becomes we were taking a limit amount of them
        historyMessages.reverse(true).removeAll { it.receiver == requestor && !it.sendDate }

        def receivedMessages = historyMessages.findAll { ChatMessage message -> requestor == message.receiver }
        receivedMessages.each { ChatMessage msg -> if (!msg.receiveDate) msg.receiveDate = new Date() }
        ChatMessage.saveAll(receivedMessages)

        historyMessages
    }

    ChatMessage sendMessage(def senderId, def receiverId, def content, MessageType type, Long msgId = null) {
        def sender = userService.get(senderId)
        def receiver = userService.get(receiverId, true)
        ChatMessage message = msgId ?
                ChatMessage.get(msgId)
                :
                "prep${WordUtils.capitalizeFully(type.textKey)}Message"(sender, receiver, content)
        message.sendDate = sender.isBlockedBy(receiver) ? null : new Date()
        message.save()
        if (message.sendDate) {
            try {
                if (userService.loggedInUsers.containsKey(receiver.id)) {
                    tryNotifyUser(receiver, message)
                }
            } catch (Exception e) {
                log.error(e.message, e);
            }
        }
        message
    }

    def tryFlushMessagesAsync(User user) {
        //nowhere to send
        if (!user.registrationId?.trim() && !user.deviceToken?.trim()) {
            return;
        }
        log.debug("About to start async worker for ${user.name}")
        //async perform checking and sending for the user
        Thread.start("[${user.name}-FLUSH]") {
            if (user && userService.loggedInUsers.containsKey(user.id)) {
                //get hibernate session to check messages
                ChatMessage.withNewSession {
                    def unreadMessages = ChatMessage.findAllByReceiverAndReceiveDateIsNull(user)
                    //no reason to send
                    if (!unreadMessages) {
                        return;
                    }
                    log.debug("Found ${unreadMessages.size()} unread message(-s)!")
                    //get messages notifications
                    NotificationLog.withSession {
                        def notifications = NotificationLog.executeQuery(
                                "FROM NotificationLog log " +
                                        "WHERE log.notified.id = :userId " +
                                        "AND log.notification.id IN (:msgIds)",
                                [userId: user.id,
                                 msgIds: unreadMessages.id]
                        )
                        log.debug("Found ${notifications.size()} notifications for messages")
                        //leave only messages without notifications found
                        unreadMessages.removeAll(notifications.notification)
                        log.debug("${unreadMessages.size()} messages left unflushed, attempting flush!")
                        //all was sent
                        if (!unreadMessages) {
                            return;
                        }
                        unreadMessages.each { msg ->
                            tryNotifyUser(user, msg)
                        }
                    }
                }
            }
        }
    }

    private void tryNotifyUser(User receiver, ChatMessage message) {
        boolean iOSPush = receiver.deviceToken?.trim()
        boolean androidPush = receiver.registrationId?.trim()
        log.debug("Got tokens:\n" +
                "iOS: ${receiver.deviceToken}\n" +
                "Android: ${receiver.registrationId}")
        if (iOSPush || androidPush) {
            def content = message.content
            def sender = message.sender
            String messageText = { ChatMessage msg ->
                String senderName = sender.name
                if (msg instanceof TextMessage) {
                    return (shortened(msg.text))
                }
                if (msg instanceof VideoMessage) {
                    return (senderName + ' has sent you a video')
                }
                if (msg instanceof PhotoMessage) {
                    int size = content.size() //content is a collection
                    return (senderName + " has sent you ${size} photo(-s)")
                }
            }.call(message)
            Integer chatsNum = 0
            if (iOSPush) {
                try {
                    chatsNum = getChatsList(receiver).findAll { ChatMessage it -> !it.receiveDate && it.receiver.id == receiver.id }.size()
                } catch (Exception e) {
                    log.error "Error upon badging ${e.message}"
                }
                try {
                    sendAPNSNotification(receiver.deviceToken) { ApnsPayloadBuilder builder ->

                        //total message cannot exceed 250 bytes
                        builder.with {
                            alertBody = sender.name + ': ' + messageText //50-100 bytes
                            badgeNumber = chatsNum //4 bytes
                            soundFileName = 'default' //8 bytes
                            addCustomProperty('senderId', sender.id) //8 + 8 bytes
                            addCustomProperty('senderName', sender.name) //10 + 5-15 bytes
                            addCustomProperty('senderPicId', sender.pictureId) //11 + 8 bytes
                        }
                    }
                    logNotification(receiver, message, 'APNS')
                } catch (Exception e) {
                    log.error "Error APNS pushing: ${e.message}"
                }
            }
            if (androidPush) {
                try {
                    sendGCMNotification(receiver.registrationId) { Message.Builder builder ->
                        builder.with {
                            collapseKey(sender.name + '-' + androidMsgCollapseId.andIncrement)
                            timeToLive(60)
                            delayWhileIdle(true)
                            data = ['message'   : messageText
                                    , 'senderId': sender.id?.toString()] as Map<String, String>
                        }
                    }
                    logNotification(receiver, message, 'GCM')
                } catch (Exception e) {
                    log.error "Error doing the GCM: ${e.message}"
                }
            }
        }
    }

    def logNotification(User user, ChatMessage message, String service = null) {
        //notifications get logged so as to later dump them on the person
        //in need of notifying
        def log = new NotificationLog(notified: user, notification: message, service: service)
        log.save()
    }

    private String shortened(String text) {
        if (!text) {
            return ''
        }
        if (text.length() <= MAX_MESSAGE_CHARS) {
            return text
        }
        (text.substring(0, MAX_MESSAGE_CHARS) + '...')
    }

    private TextMessage prepTextMessage(User sender, User receiver, String text) {
        TextMessage textMessage = new TextMessage(sender: sender, receiver: receiver, text: text)
        textMessage.save()
    }

    private PhotoMessage prepPictureMessage(User sender, User receiver, Collection<CommonsMultipartFile> photos) {
        PhotoAlbum album = mediaService.saveAsAlbum(photos)
        PhotoMessage photoMessage = new PhotoMessage(sender: sender, receiver: receiver, photoAlbum: album)
        photoMessage.save()
    }

    private VideoMessage prepVideoMessage(User sender, User receiver, CommonsMultipartFile video) {
        Video vidFile = mediaService.saveAsMP4Video(video)
        VideoMessage videoMessage = new VideoMessage(sender: sender, receiver: receiver, video: vidFile)
        videoMessage.save()
    }


    def getChatsList(User user) {
        //its important to sort in the order opposite of the one we want
        List<ChatMessage> messages = ChatMessage.createCriteria().list {
            or {
                eq('sender.id', user.id)
                eq('receiver.id', user.id)
            }
            order('sendDate', 'asc')
        } as List<ChatMessage>

        //filter away those who blocked us
        messages = messages.findAll { ChatMessage msg ->
            boolean isSender = msg.sender == user
            def prop = isSender ? 'receiver' : 'sender'
            boolean pass = false
            User.withNewSession {
                pass = !msg."${prop}"?.blocked?.contains(user)
            }
            pass
        }

        //the map wll keep merging messages into each other until the last one
        //remains, which works due to sort order
        def recentMap = messages.collectEntries {
            def keyToCheck = (it?.sender?.id == user?.id ? it?.receiver?.id : it?.sender?.id)
            [(keyToCheck): it]
        }

        recentMap.values().collect().sort { ChatMessage a, ChatMessage b -> b.sendDate <=> a.sendDate }
    }

    def getUnreadCount(def senderId, def receiverId) {
        ChatMessage.countBySenderAndReceiverAndReceiveDateIsNull(User.get(senderId), User.get(receiverId))
    }
}