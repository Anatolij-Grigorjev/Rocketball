package lt.mediapark.rocketball

class BasicFilters {

    def filters = {
        printStuff(controller: '*', action: '*') {
            before = {
                log.info("REQUEST:\n"
                        + "------------------------------------------------------------\n"
                        + "DESTINATION: ${request.requestURL.append(request.queryString ?: '')}\n"
                        + "HEADERS: ${request.getHeaderNames().collect { it + "=" + request.getHeader(it) }}\n"
                + "${request.JSON? "JSON: ${request.JSON}" : ""}")
            }
            after = {
                log.info("RESPONSE:\n" +
                        "------------------------------------------------------------\n" +
                        "Status code: ${response.status}\n" +
                        "HEADERS: ${response.getHeaderNames().collect { it + "=" + response.getHeader(it) }}")
            }
        }
    }
}
