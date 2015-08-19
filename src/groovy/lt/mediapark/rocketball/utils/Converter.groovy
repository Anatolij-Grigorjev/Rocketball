package lt.mediapark.rocketball.utils

import groovy.util.logging.Log4j

/**
 * Created by anatolij on 22/07/15.
 */
@Log4j
class Converter {


    public static Long coerceToLong(def obj) {
        if (obj == null) return null
        Long res = null
        if (!(obj instanceof Long)) {
            if (obj instanceof String) {
                res = Long.parseLong(obj)
            } else if (obj instanceof Number) {
                res = obj.longValue()
            } else if (obj.class.isAssignableFrom(Long.class)) {
                res = (Long) obj
            } else {
                log.warn("Proceeding with id that could not be coerced into Long! ID: ${obj}, " +
                        "class: ${obj.class.name}")
            }
        } else {
            res = obj;
        }
        log.debug("Coerced ${obj} to Long ${res}")
        return res
    }

}
