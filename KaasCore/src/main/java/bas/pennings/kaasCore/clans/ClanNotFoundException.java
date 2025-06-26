package bas.pennings.kaasCore.clans;

public class ClanNotFoundException extends IllegalArgumentException {
    public ClanNotFoundException(String message) {
        super(message);
    }
}
