import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Client {

    private Input input;
    private Socket serverSocket;
    private String token;

    private InputStream inputStream;

    private BufferedReader bufferedReader;


    public static void main(String[] args){
        Client client = new Client();

        Input input = new Input();

        // Ter uma thread que está sempre a registar o input, para evitar bloqueio do programa
        Thread inputThread = new Thread(Input.readNewInput(input));
        inputThread.start();

        client.start(input);

        inputThread.interrupt();
        System.exit(0);
    }

    private Socket getServerSocket() throws IOException {
        return new Socket("localhost", 12345);
    }

    private void communicateWithServer(String message) throws IOException {
        Socket socket = this.serverSocket != null ? this.serverSocket : getServerSocket();

        SocketHelper.sendSocketMessage(socket, message);
        List<String> messageList = Message.parseMessage(SocketHelper.readSocketMessage(socket));

        interpretResponse(messageList, socket);
    }
    private void start(Input input){
        this.input = input;
        try {

            String message = String.valueOf(MessageType.BEGIN_CONNECTION);

            communicateWithServer(message);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Interpretar a mensagem recebida pelo servidor.
     * MessageType permite saber que categoria de input devemos enviar ao servidor, permitindo assim criar mensagens úteis, porém a verificação de input válido cabe apenas a servidor.
     * */
    private void interpretResponse(List<String> messageList, Socket socket) throws IOException {

        List<String> arguments = messageList.subList(1, messageList.size());
        String response;
        MessageType messageType = MessageType.valueOf(messageList.get(0));

        this.input.clearQueue();

        System.out.println(String.join("\n", arguments));

        switch (messageType) {
            case ESTABLISHED_CONNECTION -> {
                response = this.input.readQueuePersistent();
                communicateWithServer(Message.createMessage(MessageType.AUTHENTICATION_MENU_USER_INPUT, response));
            }

            case REGISTER -> {
                ArrayList<String> register = new ArrayList<>();
                System.out.println("Please choose your username: ");

                response = this.input.readQueuePersistent();
                register.add(response);

                System.out.println("Please choose your password: ");

                response = this.input.readQueuePersistent();
                register.add(response);

                response = Message.createMessage(MessageType.REGISTER, register);
                communicateWithServer(response);
            }

            case LOGIN -> {
                ArrayList<String> login = new ArrayList<>();
                System.out.println("Please enter your username: ");

                response = this.input.readQueuePersistent();
                login.add(response);

                System.out.println("Please enter your password: ");


                response = this.input.readQueuePersistent();
                login.add(response);

                response = Message.createMessage(MessageType.LOGIN, login);
                communicateWithServer(response);
            }

            case VIEW_RANK -> {
                response = Message.createMessage(MessageType.VIEW_RANK, this.token);
                communicateWithServer(response);
            }

            case VIEW_RANK_SUCCESS -> {
                communicateWithServer(Message.createMessage(MessageType.QUEUE_MENU, token));
            }

            case RULES -> {
                communicateWithServer(Message.createMessage(MessageType.QUEUE_MENU, this.token));
            }

            case TOKEN_LOGIN -> {
                String token = this.input.readQueuePersistent();

                response = Message.createMessage(MessageType.TOKEN_LOGIN, token);
                communicateWithServer(response);
            }

            case REGISTER_FAIL, LOGIN_FAIL, TOKEN_FAIL -> communicateWithServer(String.valueOf(MessageType.BEGIN_CONNECTION));

            case LOGIN_SUCCESS, REGISTER_SUCCESS, TOKEN_SUCCESS, LOGOUT_FAIL, ENTER_QUEUE_FAIL -> {
                this.token = arguments.get(2);
                communicateWithServer(Message.createMessage(MessageType.QUEUE_MENU, token));
            }

            case QUEUE_MENU -> {
                String input = this.input.readQueuePersistent();

                response = Message.createMessage(MessageType.QUEUE_MENU_USER_INPUT, input);
                communicateWithServer(response);
            }

            case TOKEN_LOGOUT -> {
                response = Message.createMessage(MessageType.TOKEN_LOGOUT, this.token);
                communicateWithServer(response);
            }

            case ENTER_QUEUE -> {
                response = Message.createMessage(MessageType.ENTER_QUEUE, this.token);
                communicateWithServer(response);
            }

            case ENTER_QUEUE_SUCCESS -> {
                this.serverSocket = socket;
                this.inputStream = socket.getInputStream();
                this.bufferedReader = new BufferedReader(new InputStreamReader(this.inputStream));

                listenToServer();

                communicateWithServer(Message.createMessage(MessageType.QUEUE_MENU, token));
            }

            case PLAY -> {
                String input = this.input.readQueuePersistent();

                response = Message.createMessage(MessageType.PLAY, input);
                communicateWithServer(response);
            }
            case PING -> {
            }

            default -> {
                System.out.println(messageType);
            }
        }
    }


    private void listenToServer() throws IOException {
        String message = SocketHelper.readSocketMessage(this.serverSocket);

        boolean gameOver = false;


        while(!gameOver){
            String firstWordOfMessage = message.split(" ", 2)[0];

            // Quando a primeira palavra da mensagem do servidor é 'Player', pedir a adivinha ao user (assumindo que as adivinhas começam com a palavra 'Player')
            if(firstWordOfMessage.equals("Player")){
                String wordleGuess = this.input.readQueuePersistent();
                SocketHelper.sendSocketMessage(serverSocket, wordleGuess);
                message = "";
            }

            else{
                // Enquanto o buffer tem mensagens para ler
                while (bufferedReader.ready()){
                    message = bufferedReader.readLine();

                    if(message.equals(Message.createMessage(MessageType.GAME_OVER, Menu.GAME_OVER))){
                        this.serverSocket = null;
                        this.inputStream = null;
                        this.bufferedReader = null;
                        gameOver = true;
                        break;
                    }

                    else {
                        System.out.println(message);
                    }
                }
            }
        }

    }
}
