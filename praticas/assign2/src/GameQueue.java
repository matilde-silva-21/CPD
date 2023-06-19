import java.net.Socket;
import java.util.LinkedHashMap;

public class GameQueue {

    private LinkedHashMap<Player, Socket> players;

    private long startTime;

    private final long RELAX_TIME = Server.RELAXATION_TIME;

    private Rank rank;

    GameQueue(LinkedHashMap<Player, Socket> players){
        this.players = players;
        this.startTime = System.currentTimeMillis(); // in seconds
        setRank();
    }

    public void setRank() {
        Rank lowestRank = Rank.DIAMOND;
        for(Player p : players.keySet()){
            if(p.getLevel() < Player.calculateLevel(lowestRank)){
                lowestRank = p.getRank();
            }
        }
        this.rank = lowestRank;
    }

    public Rank getRank() {
        return rank;
    }

    public long getStartTime() {
        return startTime;
    }

    public boolean isRelaxTimeOver(){
        return (System.currentTimeMillis() - startTime) / 1000 > RELAX_TIME;
    }

    public synchronized void addPlayer(Player player, Socket socket){
        players.put(player, socket);
        startTime = System.currentTimeMillis();
    }

    public boolean playerIsInQueue(Player player){
        return players.containsKey(player);
    }

    public int getSize(){
        return players.keySet().size();
    }

    public LinkedHashMap<Player, Socket> getPlayers(){
        return players;
    }

    public synchronized void removePlayer(Player player) {
        players.remove(player);
    }

    public synchronized void mergeQueues(GameQueue oldQueue){
        for(Player player : oldQueue.getPlayers().keySet()){
            this.players.put(player, oldQueue.getPlayers().get(player));
        }
        setRank();
        this.startTime = System.currentTimeMillis();
    }
}
