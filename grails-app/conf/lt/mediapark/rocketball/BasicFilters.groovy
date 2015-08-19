package lt.mediapark.rocketball

class BasicFilters {

    def filters = {
        printRequest(controller: '*', action: '*') {
            before = {
                log.debug("REQUEST:")
                log.debug("------------------------------------------------------------")
                log.debug("DESTINATION: ${request.requestURL.append(request.queryString ?: '')}")
                log.debug("HEADERS: ${request.getHeaderNames().collect { it + "=" + request.getHeader(it) }}")
                if (request.JSON) {
                    log.debug("JSON: ${request.JSON}")
                }
            }
        }
    }
}
