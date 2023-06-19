import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Player {
    private static final String FILE_PATH = "user_data.txt";
    private static final String DELIMITER = "@";

    private String username;
    private String password;
    private int score;

    private Rank rank;

    Player(String username, String password, int score){
        this.username = username;
        this.password = password;
        this.score = score;
    }
    public static List<Player> getAllPlayers() {
        List<Player> players = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(DELIMITER);
                if (parts.length == 3) {
                    String username = parts[0];
                    String password = parts[1];
                    int score = Integer.parseInt(parts[2]);
                    Player player = new Player(username, password, score);
                    players.add(player);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return players;
    }

    public Rank getRank() {
        int points = getScore();
        if (points >= 15000) {
            return Rank.DIAMOND;
        } else if (points >= 9000) {
            return Rank.PLATINUM;
        } else if (points >= 5000) {
            return Rank.GOLD;
        } else if (points >= 2000) {
            return Rank.SILVER;
        } else {
            return Rank.BRONZE;
        }
    }

    public static int calculateLevel(Rank rank) {
        if (rank.equals(Rank.DIAMOND)) {
            return 5;
        } else if (rank.equals(Rank.PLATINUM)) {
            return 4;
        } else if (rank.equals(Rank.GOLD)) {
            return 3;
        } else if (rank.equals(Rank.SILVER)) {
            return 2;
        } else {
            return 1;
        }
    }



    public static boolean playerExists(String username) {
        List<Player> players = getAllPlayers();
        for (Player player: players) {
            if(player.username.equals(username)){return true;}
        }
        return false;
    }

    public static Player getPlayer(String username) {
        List<Player> players = Player.getAllPlayers();
        for (Player player: players) {
            if(player.username.equals(username)){return player;}
        }
        return null;
    }

    public void setScore(int score){
        this.score = score;
    }

    public void addScore(int score){
        this.score += score;
    }

    public int getLevel(){
        Rank rank = getRank();
        return calculateLevel(rank);
    }
    public int getScore() {
        return score;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
