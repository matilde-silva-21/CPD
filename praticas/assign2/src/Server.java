import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private final HashMap<String, Player> loggedInPlayers = new HashMap<>();

    private final List<GameQueue> queues = new LinkedList<>();
    private final int NUMBER_OF_PLAYERS = 4;
    public static final int RELAXATION_TIME = 20;

    public static void main(String[] args) {
        Server server = new Server();
        server.start(12345);
    }


    private void start(int port){
        try (ServerSocket serverSocket = new ServerSocket(port)){
            ExecutorService exec = Executors.newFixedThreadPool(5);
            ExecutorService sortQueueExec = Executors.newSingleThreadExecutor();
            sortQueueExec.execute(() -> {
                while (true) {
                    try {
                        sortQueues();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            while (true){
                Socket socket = serverSocket.accept();

                exec.execute(processClientMessage(socket));
            }
        }
        catch (IOException e){
            throw new RuntimeException();
        }
    }

    private void sortQueues() throws IOException {
        if(queues.size() < 1) return;

        GameQueue currentQueue = new GameQueue(new LinkedHashMap<>());
        boolean mergeNeeded = false;
        for(int i = 0; i < queues.size(); i++){
            if(queues.get(i).isRelaxTimeOver() && queues.get(i).getSize() < NUMBER_OF_PLAYERS){
                currentQueue = queues.get(i);
                mergeNeeded = true;
                break;
            }
        }
        if(!mergeNeeded)
            return;

        for(GameQueue queue : queues){
            if(queue.equals(currentQueue))
                continue;
            if (checkConditionsToMerge(currentQueue, queue)){
                currentQueue.mergeQueues(queue);
                queues.remove(queue);

                if(currentQueue.getSize() == NUMBER_OF_PLAYERS){
                    rankModeQueueReady(currentQueue);
                }
                return;
            }

        }
    }

    private boolean checkConditionsToMerge(GameQueue firstQueue, GameQueue secondQueue){
        int maxLevelDifference = (int) Math.floorDiv((System.currentTimeMillis() - firstQueue.getStartTime()) / 1000, RELAXATION_TIME);
        System.out.println(maxLevelDifference);
        return (Player.calculateLevel(secondQueue.getRank()) - Player.calculateLevel(firstQueue.getRank()) <= maxLevelDifference
                || Player.calculateLevel(firstQueue.getRank()) - Player.calculateLevel(secondQueue.getRank()) <= maxLevelDifference)
                && !(secondQueue.getSize() + firstQueue.getSize() > NUMBER_OF_PLAYERS);
    }

    /**
     * Thread sempre a escutar novas mensagens
     * */
    private Runnable processClientMessage(Socket socket){
        return () -> {
            System.out.println("Received a message from "+socket);
            try {
                String clientMessage = SocketHelper.readSocketMessage(socket);
                System.out.println("    \""+clientMessage+"\"\n");

                parseClientMessage(socket, clientMessage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    /**
     * Verifica se uma dada token já existe
     * */
    private boolean checkForToken(String token){
        return loggedInPlayers.containsKey(token);
    }

    /**
     * Gerar uma token, garantindo que mais nenhum user a tem
     * */
    private String generateUniqueToken(){
        String token = TokenGenerator.generateToken();
        while(checkForToken(token)){
            token = TokenGenerator.generateToken();
        }
        return token;
    }

    /**
     * Verificar se um dado player já está logged in
     * */
    private boolean playerIsLoggedIn(Player player){
        for (Map.Entry<String, Player> entry : loggedInPlayers.entrySet()) {
            if (entry.getValue().getUsername().equals(player.getUsername())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adicionar um player aos Logged In Players. Synchronized para evitar problemas de concorrência
     * */
    private synchronized void addToLoggedInPlayers(String token, Player player){
        loggedInPlayers.put(token, player);
    }

    /**
     * Adicionar um player à queue. Synchronized para evitar problemas de concorrência
     *
    private synchronized void addToQueuedPlayers(Player player, Socket socket){
        queuedPlayers.put(player, socket);
    }

    private boolean tokenIsInQueue(String token){
        return queuedPlayers.containsKey(loggedInPlayers.get(token));
    }

    *
     * Fazer logout de um utilizador -> remover de logged in players + enviar mensagem de logout ao player
     * */
    private synchronized String logoutUser(String token){
        loggedInPlayers.remove(token);
        return Message.createMessage(MessageType.LOGOUT_SUCCESS, Menu.LOGOUT_SUCCESS);
    }


    /**
     * Interpretar a mensagem recebida no "socket". De notar, não temos conhecimento nenhum sobre o remetente da mensagem, por isso MessageType guia todas as ações
     * */
    private void parseClientMessage(Socket socket, String message) throws IOException {
        List<String> parsedMessage = Message.parseMessage(message);
        MessageType messageType = MessageType.valueOf(parsedMessage.get(0));
        String responseString;

        switch (messageType) {
            // Se for a primeira mensagem que o cliente envia ao servidor (BEGIN_CONNECTION), enviar de volta o Menu de Autenticação
            case BEGIN_CONNECTION -> {
                responseString = Message.createMessage(MessageType.ESTABLISHED_CONNECTION, Menu.LOGIN_MENU);
                SocketHelper.sendSocketMessage(socket, responseString);
            }

            // Resposta ao Menu de Autenticação
            case AUTHENTICATION_MENU_USER_INPUT -> {
                String choice = parsedMessage.get(1);
                // Se for um input inválido
                if (!Objects.equals(choice, "1") && !Objects.equals(choice, "2") && !Objects.equals(choice, "3") && !Objects.equals(choice, "4") && !Objects.equals(choice, "5")) {
                    responseString = Message.createMessage(MessageType.ESTABLISHED_CONNECTION, Menu.AUTHENTICATION_MENU_INPUT_ERROR);
                    SocketHelper.sendSocketMessage(socket, responseString);
                }
                // Register
                else if (Objects.equals(choice, "1")) {
                    SocketHelper.sendSocketMessage(socket, String.valueOf(MessageType.REGISTER));
                }
                // Login with username
                else if (Objects.equals(choice, "2")) {
                    SocketHelper.sendSocketMessage(socket, String.valueOf(MessageType.LOGIN));
                }
                // Login with token
                else {
                    SocketHelper.sendSocketMessage(socket, Message.createMessage(MessageType.TOKEN_LOGIN, Menu.TOKEN_REQUEST));
                }
            }

            case REGISTER -> {
                String registerResponse = Authentication.registerUser(parsedMessage.get(1), parsedMessage.get(2));
                List<String> args = new ArrayList<>();

                String token = generateUniqueToken();
                args.add(registerResponse);

                if (Message.parseMessage(registerResponse).get(0).equals(String.valueOf(MessageType.REGISTER_SUCCESS))) {
                    addToLoggedInPlayers(token, Player.getPlayer(parsedMessage.get(1)));
                    args.add(Menu.TOKEN_SEND+"@"+token);
                }

                SocketHelper.sendSocketMessage(socket, Message.createMessage(args));
            }

            case LOGIN -> {
                String loginResponse = Authentication.loginUser(parsedMessage.get(1), parsedMessage.get(2));
                Player player = Player.getPlayer(parsedMessage.get(1));
                if (Message.parseMessage(loginResponse).get(0).equals(String.valueOf(MessageType.LOGIN_FAIL))) {
                    SocketHelper.sendSocketMessage(socket, loginResponse);
                } else if (playerIsLoggedIn(player)) {
                    SocketHelper.sendSocketMessage(socket, Message.createMessage(MessageType.LOGIN_FAIL, Menu.LOGIN_ACTION_ERROR));
                } else {
                    List<String> args = new ArrayList<>();
                    String token = generateUniqueToken();

                    addToLoggedInPlayers(token, player);

                    args.add(loginResponse);
                    args.add(Menu.TOKEN_SEND+"@"+token);

                    SocketHelper.sendSocketMessage(socket, Message.createMessage(args));
                }
            }

            case TOKEN_LOGIN -> {
                boolean tokenExists = checkForToken(parsedMessage.get(1));
                if (tokenExists) {
                    List<String> args = new ArrayList<>();
                    args.add("");
                    args.add(Menu.TOKEN_SUCCESS);
                    args.add(parsedMessage.get(1));
                    SocketHelper.sendSocketMessage(socket, Message.createMessage(MessageType.TOKEN_SUCCESS, args));
                } else {
                    SocketHelper.sendSocketMessage(socket, Message.createMessage(MessageType.TOKEN_FAIL, Menu.TOKEN_FAIL));
                }
            }

            // Após o user estar logged in, enviar Queue Menu
            case QUEUE_MENU -> {
                boolean TokenInQueue = false;
                for(GameQueue queue : queues){
                    if(queue.playerIsInQueue(loggedInPlayers.get(parsedMessage.get(1)))){
                        TokenInQueue = true;
                        SocketHelper.sendSocketMessage(socket, Message.createMessage(MessageType.ENTER_QUEUE_SUCCESS, Menu.ENTER_QUEUE_SUCCESS));
                    }
                }
                if(!TokenInQueue){
                    SocketHelper.sendSocketMessage(socket, Message.createMessage(MessageType.QUEUE_MENU, Menu.QUEUE_MENU));
                }
            }

            // Resposta do utilizador a Queue Menu
            case QUEUE_MENU_USER_INPUT -> {
                String choice = parsedMessage.get(1);

                // Se for uma opção inválida
                if (!Objects.equals(choice, "1") && !Objects.equals(choice, "2") && !Objects.equals(choice, "3") && !Objects.equals(choice, "4")) {
                    responseString = Message.createMessage(MessageType.QUEUE_MENU, Menu.QUEUE_MENU_INPUT_ERROR);
                    SocketHelper.sendSocketMessage(socket, responseString);
                }
                // Juntar à Game Queue
                else if (Objects.equals(choice, "1")) {
                    SocketHelper.sendSocketMessage(socket, String.valueOf(MessageType.ENTER_QUEUE));
                }
                // Ver pontos e ranqueada
                else if(Objects.equals(choice, "2")){
                    SocketHelper.sendSocketMessage(socket, String.valueOf(MessageType.VIEW_RANK));
                }
                // Ver regras
                else if(Objects.equals(choice, "3")){
                    SocketHelper.sendSocketMessage(socket, Message.createMessage(MessageType.RULES, Menu.RULES));
                }
                // Logout
                else {
                    SocketHelper.sendSocketMessage(socket, String.valueOf(MessageType.TOKEN_LOGOUT));
                }

            }

            // Juntar player à queue
            case ENTER_QUEUE -> {
                String token = parsedMessage.get(1);

                boolean tokenExists = checkForToken(token);
                if (tokenExists) {
                    Player player = loggedInPlayers.get(token);
                    for(GameQueue queue : queues){
                        if(queue.getRank() == player.getRank() && queue.getSize() < NUMBER_OF_PLAYERS){
                            queue.addPlayer(player, socket);
                            SocketHelper.sendSocketMessage(socket, Message.createMessage(MessageType.ENTER_QUEUE_SUCCESS, Menu.ENTER_QUEUE_SUCCESS));
                            if(queue.getSize() == NUMBER_OF_PLAYERS)
                                rankModeQueueReady(queue);
                            return;
                        }
                    }
                    LinkedHashMap<Player, Socket> newPlayerMap = new LinkedHashMap<>();
                    newPlayerMap.put(player, socket);
                    queues.add(new GameQueue(newPlayerMap));
                    SocketHelper.sendSocketMessage(socket, Message.createMessage(MessageType.ENTER_QUEUE_SUCCESS, Menu.ENTER_QUEUE_SUCCESS));
                }
                else {
                    SocketHelper.sendSocketMessage(socket, Message.createMessage(MessageType.ENTER_QUEUE_FAIL, Menu.ACTION_ERROR));
                }
            }

            case VIEW_RANK -> {
                String token = parsedMessage.get(1);
                Player player = loggedInPlayers.get(token);
                String  messageRank = "Showing Points and Rank for " + player.getUsername() + "...";

                int points = player.getScore();
                Rank rank = player.getRank();

                messageRank += "@Points: " + points;
                messageRank += "@Rank: " + rank;

                SocketHelper.sendSocketMessage(socket, Message.createMessage(MessageType.VIEW_RANK_SUCCESS, messageRank));
            }

            case PING -> {
                sortQueues();
                SocketHelper.sendSocketMessage(socket, Message.createMessage(MessageType.ENTER_QUEUE_SUCCESS, Menu.ENTER_QUEUE_SUCCESS));
            }


            // Logout
            case TOKEN_LOGOUT -> {
                String token = parsedMessage.get(1);
                boolean tokenExists = checkForToken(token);
                if (tokenExists) {
                    responseString = logoutUser(token);
                    SocketHelper.sendSocketMessage(socket, responseString);

                } else {
                    SocketHelper.sendSocketMessage(socket, Message.createMessage(MessageType.LOGOUT_FAIL, Menu.ACTION_ERROR));
                }
            }

            default -> {
                System.out.println(message);
            }

        }
    }

    /**
    private synchronized LinkedHashMap<Player, Socket> getFirstNPlayers(List<Player> players, int numberOfPlayers) {
        LinkedHashMap<Player, Socket> resultMap = new LinkedHashMap<>();
        List<Player> playerToBeRemoved = new ArrayList<>();
        int count = 0;

        Iterator<Player> iterator = players.iterator();
        while (iterator.hasNext() && count < numberOfPlayers) {
            Player player = iterator.next();
            resultMap.put(player, queuedPlayers.get(player));
            playerToBeRemoved.add(player);
            count++;
        }

        for (Player player : playerToBeRemoved) {
            queuedPlayers.remove(player);
            players.remove(player);
        }

        return resultMap;
    }

    /**
     * Função verifica se a queue tem elementos suficientes (Rank Mode). Em caso postivo, começa nova instância e avisa os jogadores do início de jogo
     * */
    private void rankModeQueueReady(GameQueue queue) throws IOException {
        LinkedHashMap<Player, Socket> playersHashMap = new LinkedHashMap<>(queue.getPlayers());

        List<Player> players = new ArrayList<>(playersHashMap.keySet());
        players.sort(Comparator.comparingInt(Player::getScore));

        // Group players into levels
        Map<Rank, List<Player>> playerLevels = new HashMap<>();
        for (Player player : players) {
            Rank rank = player.getRank();
            playerLevels.computeIfAbsent(rank, k -> new ArrayList<>()).add(player);
            queue.removePlayer(player);
        }

        System.out.println("New game instance created with players:\n   " + playersHashMap + "\n");

        boolean success = createNewGameInstance(playersHashMap);

        if (!success) {
            SocketHelper.sendMessageToPlayers(queue.getPlayers(), Message.createMessage(MessageType.SERVER_ERROR, Menu.ACTION_ERROR));
        }




        /* Match players with slightly different ranks within the allowed level difference
        for (int i = 0; i < remainingPlayers.size(); i++) {
            Player player1 = remainingPlayers.get(i);
            for (int j = i + 1; j < remainingPlayers.size(); j++) {
                Player player2 = remainingPlayers.get(j);
                int levelDifference = Math.abs(player1.getLevel() - player2.getLevel());
                if (levelDifference <= maxLevelDifference) {
                    LinkedHashMap<Player, Socket> gamePlayers = new LinkedHashMap<>();
                    gamePlayers.put(player1, queuedPlayers.get(player1));
                    gamePlayers.put(player2, queuedPlayers.get(player2));

                    removeFromQueues(player1);
                    removeFromQueues(player2);

                    System.out.println("New game instance created with players:\n   " + gamePlayers + "\n");

                    boolean success = createNewGameInstance(gamePlayers);

                    if (!success) {
                        SocketHelper.sendMessageToPlayers(gamePlayers, Message.createMessage(MessageType.SERVER_ERROR, Menu.ACTION_ERROR));
                    }
                }
            }
        }
        */
    }




    private boolean createNewGameInstance(LinkedHashMap<Player, Socket> players) throws IOException {
        ExecutorService exec = Executors.newFixedThreadPool(10);

        exec.execute(new Wordle(players, exec).start());

        return true;
    }
}

//TODO LIST:

/*

    Implementar rankModeQueueReady()
    Quando o jogo começar verificar que todos estão lá, porque se alguem estiver disconnected, pode merdar
    Testar token login quando o jogador já está na fila (verificar que a token fica guardada no cliente)

    Quando re-escrever as scores meter uma lock, para as threads nao mexerem umas com as outras
    Mostrar o acumulo de pontos entre cada scoreboard
    fazer o diagrama de classes para por no readme
*/
