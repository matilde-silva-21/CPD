import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

public class Input {
    private final Queue<String> queue = new LinkedList<>();

    public synchronized void addToQueue(String input) {
        queue.add(input);
    }

    /**
     * Lê input do cliente. Input de "\n" ou "" não é adicionado à queue. O user não pode usar o caracter "@", pois o servidor usa-o como delimitador
     * */
    public static Runnable readNewInput(Input input){
        return () -> {
            Scanner scanner = new Scanner(System.in);
            while(scanner.hasNext()){
                String line = scanner.next();
                if(line.contains("@")){
                    System.out.println("Input can't contain @ character. Please re-write:");
                }
                else if(!(line.equals("") || line.equals("\n"))){
                    input.addToQueue(line);
                }
            }
        };
    }

    public synchronized void clearQueue(){
        queue.clear();
    }

    /**
     * Retorna o input até agora lido. Se não houver input, retorna null.
     * */
    public synchronized String readQueue(){
        return queue.poll();
    }

    /**
     * Retorna o input até agora lido. Se não houver input, bloqueia até ler alguma coisa.
     * */
    public String readQueuePersistent(){
        String response = null;
        while (response == null) {
            response = readQueue();
        }
        return response;
    }

    public synchronized boolean empty(){
        return queue.isEmpty();
    }

}
