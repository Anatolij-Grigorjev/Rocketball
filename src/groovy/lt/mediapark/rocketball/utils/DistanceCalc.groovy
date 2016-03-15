package lt.mediapark.rocketball.utils

import groovy.transform.CompileStatic
import lt.mediapark.rocketball.User

@CompileStatic
class DistanceCalc {

  public static Double getHavershineDistance(Double lat1, Double lng1, Double lat2, Double lng2) {
    if (lat1 == null || lat2 == null || lng1 == null || lng2 == null) {
      return Double.MAX_VALUE
    }
    Double R = 6371 * 1000; // Radius of the earth in m
    Double dLat = degToRad(lat2 - lat1);  //calc in rad
    Double dLon = degToRad(lng2 - lng1);
    Double a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(degToRad(lat1)) * Math.cos(degToRad(lat2)) *
        Math.sin(dLon / 2) * Math.sin(dLon / 2)
    ;
    Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    Double d = R * c; // Distance in m
    return d;
  }

  public static Double getHavershineDistance(User user1, User user2) {
    if (!user1 || !user2) {
      return Double.MAX_VALUE
    }
    if (!user1.currLng || !user1.currLat || !user2.currLat || !user2.currLng) {
      return Double.MAX_VALUE
    }
    return getHavershineDistance(user1.currLat, user1.currLng, user2.currLat, user2.currLng)
  }


  private static Double degToRad(Double deg) {
    return deg * (Math.PI / 180);
  }

}
