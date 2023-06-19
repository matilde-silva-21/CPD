import java.io.FileWriter;
import java.io.IOException;
import java.util.List;


public class Authentication {
    private static final String FILE_PATH = "user_data.txt";
    private static final String DELIMITER = "@";


    public synchronized static String registerUser(String username, String password) {
        if (Player.playerExists(username)) {
            return Message.createMessage(MessageType.REGISTER_FAIL, Menu.REGISTER_ACTION_USERNAME_ERROR);
        }

        try (FileWriter writer = new FileWriter(FILE_PATH, true)) {
            String userEntry = username + DELIMITER + password + DELIMITER + "0\n";
            writer.write(userEntry);
            return Message.createMessage(MessageType.REGISTER_SUCCESS, Menu.REGISTER_SUCCESS);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return Message.createMessage(MessageType.REGISTER_FAIL, Menu.ACTION_ERROR);
    }


    public static String loginUser(String username, String password) {
        if(!Player.playerExists(username)){
            return Message.createMessage(MessageType.LOGIN_FAIL, Menu.LOGIN_ACTION_USERNAME_ERROR);
        }

        List<Player> players = Player.getAllPlayers();
        for (Player player: players) {
            if(player.getUsername().equals(username) && player.getPassword().equals(password)){
                return Message.createMessage(MessageType.LOGIN_SUCCESS, Menu.LOGIN_SUCCESS);
            }
        }

        return Message.createMessage(MessageType.LOGIN_FAIL, Menu.LOGIN_ACTION_PASSWORD_ERROR);
    }


}
