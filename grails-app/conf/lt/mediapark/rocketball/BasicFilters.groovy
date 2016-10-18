package lt.mediapark.rocketball

class BasicFilters {

    def filters = {
        printStuff(controller: '*', action: '*') {
            before = {
                log.debug("REQUEST:\n"
                        + "------------------------------------------------------------\n"
                        + "DESTINATION: ${request.requestURL.append(request.queryString ?: '')}\n"
                        + "HEADERS: ${request.getHeaderNames().collect { it + "=" + request.getHeader(it) }}\n")
                if (request.JSON) {
                    log.info "RQ_JSON: ${request.JSON}"
                }
            }
            after = {
                log.debug("RESPONSE:\n" +
                        "------------------------------------------------------------\n" +
                        "Status code: ${response.status}\n" +
                        "HEADERS: ${response.getHeaderNames().collect { it + "=" + response.getHeader(it) }}\n")
            }
        }
    }
}
