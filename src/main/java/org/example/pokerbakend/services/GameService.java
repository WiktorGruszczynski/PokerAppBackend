package org.example.pokerbakend.services;

import org.example.pokerbakend.services.models.Card;
import org.example.pokerbakend.services.models.Player;
import org.example.pokerbakend.services.models.Spectator;
import org.example.pokerbakend.services.models.messages.ActionMessage;
import org.example.pokerbakend.services.models.messages.TokenMessage;
import org.example.pokerbakend.services.models.messages.UpdateMessage;
import org.example.pokerbakend.services.models.messages.join.JoinResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

//To jest model z MVC
@Service
public class GameService{
    private final SecurityService securityService;
    private final SimpMessagingTemplate messagingTemplate;
    private final PokerHandService pokerHandService;

    private final List<Spectator> spectators = new ArrayList<>();
    private final List<Player> players = new ArrayList<>();
    private final Dealer dealer;
    private final int MAX_PLAYERS = 5;
    private Player currentPlayer;
    private Integer tableBet = 0;
    private final List<Card> tableCards = new ArrayList<>();
    private String updateMessage = "update";

    private boolean running = false;
    private boolean awaitingMove = false;


    public GameService(SimpMessagingTemplate messagingTemplate, JsonReaderService jsonReaderService, SecurityService securityService, PokerHandService pokerHandService, PokerHandService pokerHandService1) {
        this.messagingTemplate = messagingTemplate;
        this.securityService = securityService;
        this.dealer = new Dealer(
            jsonReaderService.loadCards()
        );
        this.pokerHandService = pokerHandService1;
    }

    public JoinResponse joinGame(String username) {
        if (players.size() < MAX_PLAYERS && !running) {
            String token = securityService.generateToken();
            int id = securityService.generateId();

            int STARTING_BALANCE = 10_000;
            Player player = new Player(
                    id,
                    username,
                    token,
                    STARTING_BALANCE
            );
            players.add(player);


            return new JoinResponse(
                    true,
                    "Connected successfully",
                    token,
                    player
            );
        }
        else{
            return new JoinResponse(
                    false,
                    "Couldn't connect"
            );
        }
    }

    private boolean isValidPlayer(Player player) {
        for (Player p : players) {
            if (p.getId().equals(player.getId())) {
                return true;
            }
        }
        return false;
    }

    public List<Card> getHand(String token) {
        for (Player player: players){
            if (player.getToken().equals(token)) {
                return player.getHand();
            }
        }
        return null;
    }

    public void startGame(Player player) {
        if (isValidPlayer(player)) {
            if (players.size() < MAX_PLAYERS && players.size() > 1) {
                running = true;
                mainLoop();
            }
        }
    }

    private void awaitPlayerMove(){
        awaitingMove = true;

        while (awaitingMove) {
            try {
                Thread.sleep(30);
            }
            catch (InterruptedException e){
                throw new RuntimeException(e);
            }
        }

    }

    private void awaitAllPlayersReady(){
        while (!isEveryPlayerReady()) {
            try {
                Thread.sleep(30);
            }
            catch (InterruptedException e){
                throw new RuntimeException(e);
            }
        }

        setupNextGame();
    }

    private boolean isEveryPlayerReady(){
        for (Player player : players) {
            if (player.getStatus().equals("winner") || player.getStatus().equals("waiting")) {
                return false;
            }
        }
        return true;
    }

    public void action(ActionMessage message) {
        if (currentPlayer.getToken().equals(message.getToken())) {
            switch (message.getAction()) {
                case "fold": {
                    currentPlayer.fold();
                    break;
                }
                case "raise": {
                    int amount = message.getAmount();
                    currentPlayer.raise(amount, tableBet);
                    tableBet = amount;
                    break;
                }
                case "check": {
                    currentPlayer.check(tableBet);
                    break;
                }
                case "call": {
                    currentPlayer.call(tableBet);
                    break;
                }
            }

            awaitingMove = false;
        }
    }

    private void dealCardsToPlayers(){
        for (Player player : players) {
            player.setHand(dealer.dealCards(2));
        }
    }

    private void dealCardsToTable(){
        if (countActivePlayers()==1){
            return;
        }

        if (tableCards.isEmpty()) {
            tableCards.addAll(dealer.dealCards(3));
        }
        else if (tableCards.size() == 3 || tableCards.size() == 4) {
            tableCards.addAll(dealer.dealCards(1));
        }
    }

    private void updateGame(){
        messagingTemplate.convertAndSend(
                "/topic/update",
                new UpdateMessage(
                        currentPlayer,
                        tableCards,
                        players,
                        updateMessage
                )
        );
    }

    private boolean isRoundFinished(){
        for (Player player : players) {
            String playerStatus = player.getStatus();

            if (playerStatus.equals("fold")) continue;
            if (!playerStatus.equals("checked")) return false;
        }
        return true;
    }

    private void reloadPlayersStatus(){
        for (Player player : players) {
            if (player.getStatus().equals("checked")){
                player.setStatus("active");
            }
        }
    }

    private int countActivePlayers(){
        int counter = 0;
        for (Player player : players) {
            String status = player.getStatus();
            if (status.equals("active") || status.equals("checked")) counter++;
        }

        return counter;
    }

    private void round(){
        do {
            for (Player player : players) {
                if (player.getStatus().equals("fold")) {
                    continue;
                }

                if (countActivePlayers()==1){
                    return;
                }

                currentPlayer = player;

                updateGame();
                awaitPlayerMove();
            }

            updateGame();
        } while (!isRoundFinished());

        reloadPlayersStatus();
    }

    public List<Player> getWinner() {
        if (countActivePlayers()==1){
            for (Player player : players) {
                if (player.getStatus().equals("active") || player.getStatus().equals("checked")){
                    return List.of(player);
                }
            }
            return null;
        }
        else{
            for (Player player: players){
                player.setHandEvaluation(pokerHandService.evaluateHand(player.getHand(), tableCards));
            }

            return pokerHandService.comparePlayersHands(players);
        }
    }

    private int calculatePrize(){
        int prize = 0;

        for (Player player : players) {
            prize+=player.getBet();
            player.setBet(0);
        }

        return prize;
    }

    private void finishGame(){
        currentPlayer = new Player(0,"","",0);
        List<Player> winners = getWinner();
        int prize = calculatePrize();

        for (Player winner : winners) {
            for (Player player : players) {
                if (player.getId().equals(winner.getId())) {
                    player.addBalance(prize/winners.size());
                    player.setStatus("winner");
                }
                else{
                    player.setStatus("waiting");
                }
            }
        }

        updateMessage = "finish";
        updateGame();
    }

    private void setupNextGame() {
        dealer.resetDeck();
        for (Player player : players) {
            player.setStatus("active");
            player.setHandEvaluation(null);
        }

        tableCards.clear();
        currentPlayer = players.getFirst();
        tableBet=0;
        updateMessage = "update";
        updateGame();
    }

    private void mainLoop(){
        while (running) {
//            Rozdanie kart graczom
            dealCardsToPlayers();

//            Pierwsza runda
            dealCardsToTable();
            round();

//            Druga runda
            dealCardsToTable();
            round();

//            Trzecia runda
            dealCardsToTable();
            round();

            finishGame();
            awaitAllPlayersReady();
        }
    }

    public void setPlayerReady(TokenMessage tokenMessage) {
        for (Player player: players){
            if (player.getToken().equals(tokenMessage.getToken())){
                player.setStatus("ready");
            }
        }
    }

    public boolean isGameRunning() {
        return running;
    }

    public Spectator addSpectator() {
        Spectator spectator = new Spectator(securityService.generateId());

        spectators.add(spectator);

        return spectator;
    }

    public void fetchUpdate() {
        updateGame();
    }
}