import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Wordle {
    private static final int MAX_ATTEMPTS = 6;
    private static final int ROUNDS = 5;

    private static final String WORDS_FILE = "words5.txt";
    private static final String USER_DATA_FILE = "user_data.txt";

    private static List<String> WORDS;

    private final ExecutorService executor;

    private final LinkedHashMap<Player, Socket> queuedPlayers;
    private final Object roundLock = new Object();
    private int waitingPlayers;


    private int position;


    public Wordle(LinkedHashMap<Player, Socket> playerWriters, ExecutorService executor) {

        this.queuedPlayers = playerWriters;
        this.executor = executor;
        WORDS = loadWordsFromFile();
    }


    public Runnable start() throws IOException {
        return () -> {
            try{
                sendToAllPlayers("The game will begin in:");
                for (int i = 3; i > 0; i--) {
                sendToAllPlayers(i + "...");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                sendToAllPlayers("Go!");
                for (int round = 1; round <= ROUNDS; round++) {

                    if(round == ROUNDS){
                        sendToAllPlayers("Starting Final round");
                    } else {
                        sendToAllPlayers("Starting round " + round);
                    }
                    String secretWord = getRandomWord();

                    waitingPlayers = queuedPlayers.size();

                    position = 0;

                    List<PlayerResult> results = new ArrayList<>();
                    List<MyFuture<PlayerResult>> futures = new ArrayList<>();

                    for (Map.Entry<Player, Socket> playerWriter : queuedPlayers.entrySet()) {
                        MyFuture<PlayerResult> future = new MyFuture<>();
                        futures.add(future);

                        executor.submit(() -> {
                            PlayerResult result = null;
                            try {
                                result = playRound(playerWriter, secretWord);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            future.setResult(result);
                        });
                    }

                    for (MyFuture<PlayerResult> future : futures) {
                        try {
                            results.add(future.get());
                        } catch (InterruptedException e) {
                            System.err.println("Error during round: " + e.getMessage());
                        }
                    }

                    if(round == ROUNDS) {
                        sendToAllPlayers("End of Final Round");
                    } else {
                        sendToAllPlayers("End of Round " + round);
                    }


                    // Calculate scores and update player points
                    Collections.sort(results);
                    updatePlayerPoints(results);

                    // Display the scoreboard
                    displayScoreboard(new ArrayList<>(results));
                }
                sendToAllPlayers("Game Over!");


                String message = Message.createMessage(MessageType.GAME_OVER, Menu.GAME_OVER);
                sendToAllPlayers(message);

                executor.shutdown();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public void displayScoreboard(List<PlayerResult> players) throws IOException {
        Collections.sort(players, Comparator.comparingInt(PlayerResult::getScore).reversed());
        sendToAllPlayers("Scoreboard:");
        for (PlayerResult player : players) {
            sendToAllPlayers(player.getPlayer().getUsername() + ": " + player.getScore());
        }
    }

    private static List<String> loadWordsFromFile() {
        List<String> words = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(WORDS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                words.add(line.trim());
            }
        } catch (IOException e) {
            System.err.println("Error loading words from file: " + e.getMessage());
        }

        return words;
    }

    private static String getRandomWord() {
        Random random = new Random();
        return WORDS.get(random.nextInt(WORDS.size()));
    }

    private PlayerResult playRound(Map.Entry<Player, Socket> playerWriter, String secretWord) throws IOException {
        int score = 0;
        int attemptsLeft = MAX_ATTEMPTS;
        boolean isWordGuessed = false;
        Socket socket = playerWriter.getValue();
        Player player = playerWriter.getKey();


        synchronized (roundLock) {
            waitingPlayers--;
            if (waitingPlayers > 0) {
                try {
                    roundLock.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                roundLock.notifyAll();
            }
        }
        synchronized (roundLock) {
            waitingPlayers--;
            if (waitingPlayers > 0) {
                try {
                    roundLock.wait();
                } catch (InterruptedException e) {
                    System.err.println("Error during round: " + e.getMessage());
                }
            } else {
                roundLock.notifyAll();
            }
        }

        try {
            while (!isWordGuessed && attemptsLeft > 0) {
                sendToPlayer("Player " + player.getUsername() + " - Attempt " + (MAX_ATTEMPTS - attemptsLeft + 1) + "/" + MAX_ATTEMPTS + ": ", socket);

                String guess = readPlayerMessage(socket);

                if (guess.length() != secretWord.length()) {
                    sendToPlayer("Please enter a " + secretWord.length() + "-letter word.", socket);
                } else if (!WORDS.contains(guess)) {
                    sendToPlayer("Please enter a valid " + secretWord.length() + "-letter word.", socket);
                } else {
                    if (guess.equals(secretWord)) {
                        isWordGuessed = true;
                    } else {
                        SocketHelper.sendFeedback(socket, guess, secretWord);
                        attemptsLeft--;
                    }
                }
            }
            if(attemptsLeft == 0){
                sendToPlayer("Sorry, your attempts are over. Waiting for other players...", socket);
            }
        } catch (IOException e) {
            System.err.println("Error during round: " + e.getMessage());
        }

        if (isWordGuessed) {
            SocketHelper.sendFeedback(socket, secretWord, secretWord);
            sendToPlayer("Congratulations! That's the right answer! :)", socket);
            sendToPlayer("Waiting for other players...", socket);
            attemptsLeft--;
            int playerPosition = incrementAndGetPosition();
            switch (playerPosition) {
                case 1:
                    score = 100 + (int) (attemptsLeft * 10);
                    break;
                case 2:
                    score = 50 + (int) (attemptsLeft * 10.5);
                    break;
                case 3:
                    score = 30 + (int) (attemptsLeft * 10.3);
                    break;
                default:
                    score = 5;
                    break;
            }
        }

        synchronized (roundLock) {
            waitingPlayers--;
            if (waitingPlayers > 0) {
                try {
                    roundLock.wait();
                } catch (InterruptedException e) {
                    System.err.println("Error during round: " + e.getMessage());
                }
            } else {
                roundLock.notifyAll();
            }
        }

        sendToPlayer("The answer was: " + secretWord, socket);

        return new PlayerResult(player, score, attemptsLeft);
    }

    private synchronized int incrementAndGetPosition() {
        position += 1;
        return position;
    }

    private void updatePlayerPoints(List<PlayerResult> results) {
        Map<Player, Integer> playerScores = new HashMap<>();

        int points;
        // Accumulate scores for each player
        for (PlayerResult result : results) {
            Player player = result.getPlayer();

            points = result.getScore() + player.getScore();

            player.setScore(points);

            playerScores.put(player, points);
        }

        // Update scores in the user data file
        try (BufferedReader reader = new BufferedReader(new FileReader(USER_DATA_FILE))) {
            List<String> lines = new ArrayList<>();
            String line;
            boolean updated = false; // Track if a player's score has been updated

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("@");
                if (parts.length == 3) {
                    String username = parts[0];
                    String password = parts[1];
                    int score = Integer.parseInt(parts[2]);

                    for (PlayerResult result : results) {
                        Player player = result.getPlayer();
                        if (player.getUsername().equals(username)) {
                            playerScores.put(player, player.getScore());
                            lines.add(username + "@" + password + "@" + player.getScore());
                            updated = true;
                            break; // Found the matching player, no need to continue searching
                        }
                    }
                }

                if (!updated) {
                    lines.add(line); // Add the original line if the player was not found or updated
                }

                updated = false; // Reset the flag
            }

            // Write the updated lines back to the user data file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(USER_DATA_FILE))) {
                for (String updatedLine : lines) {
                    writer.write(updatedLine);
                    writer.newLine();
                }
            }
        }
        catch (IOException e) {
            System.err.println("Error updating scores: " + e.getMessage());
        }
    }

    private  void sendToAllPlayers(String message) throws IOException {
        for (Socket writer: queuedPlayers.values()) {
            SocketHelper.sendSocketMessage(writer, message);
        }
    }

    private void sendToPlayer(String message, Socket socket) throws IOException {
        SocketHelper.sendSocketMessage(socket, message);
    }

    private String readPlayerMessage(Socket socket) throws IOException {
        String message = SocketHelper.readSocketMessage(socket);
        return message;
    }

    public class PlayerResult implements Comparable<PlayerResult> {
        private Player player;
        private int score;
        private int attemptsLeft;

        public PlayerResult(Player player, int score, int attemptsLeft) {
            this.player = player;
            this.score = score;
            this.attemptsLeft = attemptsLeft;
        }

        @Override
        public int compareTo(PlayerResult other) {
            return Integer.compare(other.score, this.score);
        }

        public int getAttemptsLeft() {
            return attemptsLeft;
        }

        public Player getPlayer() {
            return player;
        }

        public int getScore() {
            return score;
        }
    }
}