package lt.mediapark.rocketball.clsf

/**
 * Created by anatolij on 21/07/15.
 */
public enum MessageType {


    TEXT("TEXT"),
    PICTURE("PICTURE"),
    VIDEO("VIDEO");

    private final String textKey

    private MessageType(String textKey) {
        this.textKey = textKey
    }

    public String getTextKey() {
        return textKey
    }

}