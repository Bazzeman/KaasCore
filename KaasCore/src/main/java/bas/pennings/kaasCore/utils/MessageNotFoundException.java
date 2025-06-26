package bas.pennings.kaasCore.utils;

public class MessageNotFoundException extends RuntimeException {
    public MessageNotFoundException(String section, String key) {
        super("Message not found for section " + section + " and key " + key);
    }
}
