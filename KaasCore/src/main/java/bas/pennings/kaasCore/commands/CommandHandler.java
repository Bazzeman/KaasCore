package bas.pennings.kaasCore.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface CommandHandler {

    String name();

    String usage();

    String permission();

    SenderType[] senderTypes();
}