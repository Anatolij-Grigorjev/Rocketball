package lt.mediapark.rocketball

import grails.converters.JSON
import lt.mediapark.rocketball.clsf.MessageType
import lt.mediapark.rocketball.message.ChatMessage
import lt.mediapark.rocketball.utils.Constants
import org.springframework.web.multipart.MultipartRequest

class ChatController {

    def userService
    def chatService
    def converterService


    static allowedMethods = [
            index: 'GET',
            send : 'POST'
    ]

    def index() {

        def requestorId = params.requestor

        boolean requestorIs1 = params.id1 == requestorId
        //only a user (and one of the members of the chat at that!) can view chat history
        if (requestorId && (params.id1 == requestorId || params.id2 == requestorId)) {

            def user1 = userService.get(params.id1, !requestorIs1)
            def user2 = userService.get(params.id2, requestorIs1)
            Date time = params.time ?: new Date()
            Integer limit = Integer.parseInt(params.msgLmt ?: "50")

            def history = chatService.getChatHistory(
                    requestorIs1 ? user1 : user2,
                    requestorIs1 ? user2 : user1,
                    time,
                    limit
            )

            Collection<Map> messages = history.collect { converterService.chatMessageToJSON(it) }
            render messages as JSON

        } else {
            render 403
        }
    }


    def send() {

        def senderId = params.requestor
        def receiverId = params.id
        //resolve message type based on request type and files in request
        MessageType msgType = {
            if (request instanceof MultipartRequest) {
                if (request.fileMap.size() > 1) {
                    MessageType.PICTURE
                } else {
                    request.getFile(Constants.VIDEO_FILE_KEY) ? MessageType.VIDEO : MessageType.PICTURE
                }
            } else {
                MessageType.TEXT
            }
        }.call()
        //resolve payload based on request contents
        log.debug("Received message type: " + msgType.textKey)
        def content = {
            switch (msgType) {
                case MessageType.TEXT:
                    return request.JSON.text
                case MessageType.PICTURE:
                    return request.fileMap.values()
                case MessageType.VIDEO:
                    return request.fileMap[Constants.VIDEO_FILE_KEY]
            }
        }.call()

        log.debug("Got contents: ${content}")

        ChatMessage message = chatService.sendMessage(senderId, receiverId, content, msgType)
        def map = converterService.chatMessageToJSON(message)
        render map as JSON
    }


}
