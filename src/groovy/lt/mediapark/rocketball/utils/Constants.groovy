package lt.mediapark.rocketball.utils

import groovy.transform.CompileStatic

/**
 * Created by anatolij on 03/06/15.
 */
@CompileStatic
class Constants {

    public static Integer PEOPLE_RADIUS_M;
    public static Long HEARTBEAT_PERIOD_MS;
    public static Long TTL_PERIOD_MS;

    public static final String VIDEO_FILE_KEY = 'video'
    public static final String AVATAR_PICTURE_FILE_KEY = 'photo'

    public static void init(Map grails) {
        PEOPLE_RADIUS_M = ((Map<?, Integer>) grails.rocketball).radius
        HEARTBEAT_PERIOD_MS = ((Map<?, Long>) grails.rocketball).heartbeat
        TTL_PERIOD_MS = ((Map<?, Long>) grails.rocketball).ttl
    }

    public static final String[] randomWords = [
            'claught'
            , 'emotiveness'
            , 'predescribing'
            , 'battleground'
            , 'bougie'
            , 'idyllist'
            , 'paragenesia'
            , 'comedy'
            , 'stereoscopy'
            , 'bini'
            , 'methodised'
            , 'mesothoraces'
            , 'autonomous'
            , 'interradiated'
            , 'springwood'
            , 'foulness'
            , 'potman'
            , 'busload'
            , 'scheele'
            , 'grainedness'
            , 'pneumococcus'
            , 'dichotomising'
            , 'reconciling'
            , 'fixity'
            , 'toryism'
            , 'jansenistical'
            , 'quinquagenaries'
            , 'sustentational'
            , 'wearied'
            , 'slurried'
            , 'superfructified'
            , 'colloquiums'
            , 'mudfish'
            , 'nigrosin'
            , 'hoverport'
            , 'stinginess'
            , 'thorium'
            , 'preeconomic'
            , 'transpassional'
            , 'eruption'
            , 'nonconducive'
            , 'impuissance'
            , 'churidars'
            , 'absorption'
            , 'biomagnetic'
            , 'septicity'
            , 'coaldale'
            , 'phelps'
            , 'unimbibed'
            , 'soupcon'
    ]

}
