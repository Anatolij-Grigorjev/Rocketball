import grails.util.Environment

class UrlMappings {

    static mappings = {
        "/$controller/$action?/$id?(.$format)?" {
            constraints {
                // apply constraints here
            }
        }

        "/$controller/$id?" {
            action = 'index'
        }

        "/pictures/$id" {
            controller = 'media'
            action = 'index'
        }

        "/pictures/upload/$id" {
            controller = 'media'
            action = 'upload'
        }

        "/video/$id" {
            controller = 'media'
            action = 'getVideo'
        }

        "/album/$id" {
            controller = 'media'
            action = 'getPhotoAlbum'
        }

        "/reg/pic/$id?" {
            controller = 'login'
            action = 'regPicture'
        }

        "/reg/text/$id?" {
            controller = 'login'
            action = 'regText'
        }

        "/reg/app" {
            controller = 'login'
            action = 'regNative'
        }

        "/logout/$id" {
            controller = 'users'
            action = 'logout'
        }

        "/login/app" {
            controller = 'login'
            action = 'loginNative'
        }

        "/login/$id?" {
            constraints {
                id matches: "\\d+"
            }
            controller = 'login'
            action = 'loginFB'
        }

        "/resetPassword" {
            controller = 'login'
            action = 'resetPwd'
        }

        if (Environment.current != Environment.PRODUCTION) {
            "/debug/login/$id?" {
                controller = 'debug'
                action = 'login'
            }
        }

        "/users/list/$type/$id" {
            controller = 'users'
            action = 'list'
        }

        "/chat/$id1/$id2/$time?" {
            constraints {
                id1 matches: /\d+/
                id2 matches: /\d+/
                time matches: /\d+/
            }
            action = 'index'
            controller = 'chat'
        }
        "/chat/send/$id" {
            constraints {
                id matches: /\d+/
            }
            action = 'send'
            controller = 'chat'
        }

        "/"(view: "/index")
        "500"(view: '/error')
    }
}
