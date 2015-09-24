package lt.mediapark.rocketball

class BasicFilters {

    def filters = {
        printStuff(controller: '*', action: '*') {
            before = {
                log.debug("REQUEST:")
                log.debug("------------------------------------------------------------")
                log.debug("DESTINATION: ${request.requestURL.append(request.queryString ?: '')}")
                log.debug("HEADERS: ${request.getHeaderNames().collect { it + "=" + request.getHeader(it) }}")
                if (request.JSON) {
                    log.debug("JSON: ${request.JSON}")
                }
            }
            after = {
                log.debug("RESPONSE:")
                log.debug("------------------------------------------------------------")
                log.debug("Status code: ${response.status}")
                log.debug("HEADERS: ${response.getHeaderNames().collect { it + "=" + response.getHeader(it) }}")
            }
        }
    }
}
