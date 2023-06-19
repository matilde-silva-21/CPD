import java.io.*;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class SocketHelper {
    public static String readSocketMessage(Socket socket) throws IOException {
        InputStream input = socket.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(input));

        return bufferedReader.readLine();
    }

    public static boolean isReady(Socket socket) throws IOException {
        InputStream input = socket.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(input));

        return bufferedReader.ready();
    }

    public static void sendSocketMessage(Socket socket, String message) throws IOException {
        OutputStream outputStream = socket.getOutputStream();
        PrintWriter printWriter = new PrintWriter(outputStream, true);

        printWriter.println(message);

    }

    public static void sendFeedback(Socket socket, String guess, String answer) throws IOException {
        OutputStream outputStream = socket.getOutputStream();
        PrintWriter printWriter = new PrintWriter(outputStream, true);

        String feedback  = "";
        for (int i = 0; i < answer.length(); i++) {
            char secretChar = answer.charAt(i);
            char guessChar = guess.charAt(i);

            if (secretChar == guessChar) {
                feedback += printColored(String.valueOf(guessChar), "\033[32m", printWriter) ; // Green
            } else if (answer.contains(String.valueOf(guessChar))) {
                feedback += printColored(String.valueOf(guessChar), "\033[33m", printWriter); // Yellow
            } else {
                feedback += guessChar;
            }
        }

        printWriter.println(feedback);
    }

    private static String printColored(String text, String colorCode, PrintWriter printWriter) {
        String message = colorCode + text + "\033[0m";
        return message;
    }

    public static void sendMessageToPlayers(LinkedHashMap<Player, Socket> players, String message) throws IOException {
        for (Map.Entry<Player, Socket> entry : players.entrySet()) {
            sendSocketMessage(entry.getValue(), message);
        }
    }

}
