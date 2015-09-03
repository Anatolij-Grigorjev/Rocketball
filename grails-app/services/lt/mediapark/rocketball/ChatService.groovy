package lt.mediapark.rocketball

import grails.transaction.Transactional
import groovyx.gpars.GParsPool
import lt.mediapark.rocketball.clsf.MessageType
import lt.mediapark.rocketball.message.ChatMessage
import lt.mediapark.rocketball.message.PhotoMessage
import lt.mediapark.rocketball.message.TextMessage
import lt.mediapark.rocketball.message.VideoMessage
import org.apache.commons.lang.WordUtils
import org.springframework.web.multipart.commons.CommonsMultipartFile

@Transactional
class ChatService {

    def userService
    def mediaService

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
        historyMessages.reverse(true)
        def receivedMessages = historyMessages.findAll { ChatMessage message -> requestor == message.receiver }
        receivedMessages.each { ChatMessage msg -> if (!msg.receiveDate) msg.receiveDate = new Date() }
        ChatMessage.saveAll(receivedMessages)

        historyMessages
    }

    ChatMessage sendMessage(def senderId, def receiverId, def content, MessageType type) {
        def sender = userService.get(senderId)
        def receiver = userService.get(receiverId, true)

        return "send${WordUtils.capitalizeFully(type.textKey)}Message"(sender, receiver, content)
    }

    private TextMessage sendTextMessage(User sender, User receiver, String text) {
        TextMessage textMessage = new TextMessage(sender: sender, receiver: receiver, text: text, sendDate: new Date())
        textMessage.save()
    }

    private PhotoMessage sendPictureMessage(User sender, User receiver, Collection<CommonsMultipartFile> photos) {
        PhotoAlbum album = mediaService.saveAsAlbum(photos)
        PhotoMessage photoMessage = new PhotoMessage(sender: sender, receiver: receiver, photoAlbum: album)
        photoMessage.sendDate = new Date()
        photoMessage.save()
    }

    private VideoMessage sendVideoMessage(User sender, User receiver, CommonsMultipartFile video) {
        Video vidFile = mediaService.saveAsVideo(video)
        VideoMessage videoMessage = new VideoMessage(sender: sender, receiver: receiver, video: vidFile)
        videoMessage.sendDate = new Date()
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

        GParsPool.withPool {
            //filter away those who blocked us
            messages = messages.findAllParallel { ChatMessage msg ->
                boolean isSender = msg.sender == user
                def prop = isSender ? 'receiver' : 'sender'
                !msg."${prop}"?.blocked?.contains(user)
            }
        }

        //the map wll keep merging messages into each other until the last one
        //remains, which works due to sort order
        def recentMap = messages.collectEntries {
            def keyToCheck = (it?.sender?.id == user?.id ? it?.receiver?.id : it?.sender?.id)
            [(keyToCheck): it]
        }
        recentMap.values().collect()
    }
}