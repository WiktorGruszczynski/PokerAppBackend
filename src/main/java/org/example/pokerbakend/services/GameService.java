package org.example.pokerbakend.services;

import org.example.pokerbakend.services.models.Card;
import org.example.pokerbakend.services.models.Dealer;
import org.example.pokerbakend.services.models.Player;
import org.example.pokerbakend.services.models.messages.ActionMessage;
import org.example.pokerbakend.services.models.messages.UpdateMessage;
import org.example.pokerbakend.services.models.messages.join.JoinResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

//To jest model z MVC
@Service
public class GameService {
    private final SecurityService securityService;
    private final SimpMessagingTemplate messagingTemplate;

    private final int STARTING_BALANCE = 10_000;
    private final List<Player> players = new ArrayList<>();

    private final Dealer dealer;

    private final int MAX_PLAYERS = 5;


    private boolean running = false;
    private boolean awaitingMove = false;

    private Player currentPlayer;
    private Integer currentBet = 0;
    private List<Card> tableCards = new ArrayList<>();


    public GameService(SimpMessagingTemplate messagingTemplate, JsonReaderService jsonReaderService, SecurityService securityService) {
        this.messagingTemplate = messagingTemplate;
        this.securityService = securityService;
        this.dealer = new Dealer(
            jsonReaderService.loadCards()
        );
    }

    public JoinResponse joinGame(String username) {
        if (players.size() < MAX_PLAYERS && !running) {
            String token = securityService.generateToken();
            int id = securityService.generateId();

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
                Thread.sleep(10);
            }
            catch (InterruptedException e){
                throw new RuntimeException(e);
            }
        }

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
                    currentPlayer.raise(amount);
                    currentBet = amount;
                    break;
                }
                case "check": {
                    currentPlayer.check(currentBet);
                    break;
                }
                case "call": {
                    currentPlayer.call(currentBet);
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
                        currentPlayer.getId(),
                        tableCards,
                        players
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

    public Player getWinner() {
        //            1. Wygranie walkoverem
        if (countActivePlayers()==1){
            for (Player player : players) {
                if (player.getStatus().equals("active")){
                    System.out.println(player);
                    System.out.println("Wygrana walkoverem");
                    break;
                }
            }
        }
        return null;
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

            currentPlayer = null;

            getWinner();

            running = false;
        }
    }


}