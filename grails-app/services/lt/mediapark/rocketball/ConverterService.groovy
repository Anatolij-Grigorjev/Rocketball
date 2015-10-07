package lt.mediapark.rocketball

import grails.transaction.Transactional
import lt.mediapark.rocketball.message.ChatMessage
import lt.mediapark.rocketball.message.PhotoMessage
import lt.mediapark.rocketball.message.TextMessage
import lt.mediapark.rocketball.message.VideoMessage
import lt.mediapark.rocketball.utils.DistanceCalc

@Transactional
class ConverterService {

    Map userToJSON(User user, User relation = null) {
        def map = msgUserToJSON(user)

        if (user?.description != null) map['description'] = user.description
        if (user?.email) map['email'] = user.email
        if (user?.currLat) map['currLat'] = user.currLat
        if (user?.currLng) map['currLng'] = user.currLng
        if (user?.tempPassword) map['tempPassword'] = user.tempPassword
        map.putAll mapUserRelationInfo(user, relation)

        map
    }

    private Map mapUserRelationInfo(User user, User relation = null) {
        def map = [:]
        if (relation && relation != user) {
            //returning user in relation to another, so we know if they favorite/blocked
            map['favorite'] = relation?.favorites?.id?.contains(user?.id)
            map['blocked'] = relation?.blocked?.id?.contains(user?.id)

            if (user?.hasLocation() && relation?.hasLocation()) {
                map['distance'] = DistanceCalc.getHavershineDistance(user, relation)
            }
        } else {
            //returning user on their own, so we see their list of favorites/blocked
            map['favorite'] = user?.favorites?.id
            map['blocked'] = user?.blocked?.id
        }

        map
    }

    Map chatMessageToJSON(ChatMessage chatMessage) {
        def map = [:]
        if (chatMessage?.id) map['id'] = chatMessage.id
        if (chatMessage?.receiver) map['receiver'] = msgUserToJSON(chatMessage.receiver)
        if (chatMessage?.sender) map['sender'] = msgUserToJSON(chatMessage.sender)
        if (chatMessage?.sendDate) map['sendDate'] = chatMessage.sendDate.time
        if (chatMessage?.receiveDate) map['receiveDate'] = chatMessage.receiveDate.time
        //auto resolves to what is required at that moment
        if (chatMessage?.content) map['content'] = contentJSON(chatMessage)

        map
    }

    Map chatMessageToJSON(ChatMessage chatMessage, User requestor) {
        def map = chatMessageToJSON(chatMessage)
        boolean reqReceives = chatMessage.receiver == requestor
        def list = [chatMessage.sender, chatMessage.receiver]
        def users = reqReceives ? list : list.reverse()
        def key = reqReceives ? 'sender' : 'receiver'

        ((Map) map[(key)]).putAll mapUserRelationInfo(*users)

        map
    }

    def msgUserToJSON(User user) {
        def map = [:]
        if (user?.name) map['name'] = user.name
        if (user?.picture) map['picId'] = user.picture.id
        if (user?.id) map['id'] = user.id
        map['registrationId'] = user.registrationId
        map['deviceToken'] = user.deviceToken
        map
    }

    Map contentJSON(TextMessage msg) {
        def map = [:]
        if (msg?.text) map['text'] = msg.text

        map
    }

    Map contentJSON(PhotoMessage msg) {
        def map = [:]
        if (msg?.photoAlbum) map['album'] = msg?.photoAlbum?.id

        map
    }

    Map contentJSON(VideoMessage msg) {
        def map = [:]
        if (msg?.video) map['videoId'] = msg?.video?.id

        map
    }
}
