import java.util.List;

public class Message {


    /** Messages must be in format: MessageType@Arguments */
    public static List<String> parseMessage(String message){
        return List.of(message.split("@"));
    }

    public static String createMessage(MessageType messageType, List<String> arguments){
        return messageType + "@" + String.join("@", arguments);
    }

    public static String createMessage(MessageType messageType, String argument){
        return messageType + "@" + argument;
    }

    public static String createMessage(List<String> arguments){
        return String.join("@", arguments);
    }
}
