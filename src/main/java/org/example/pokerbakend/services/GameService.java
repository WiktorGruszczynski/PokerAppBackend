package org.example.pokerbakend.services;



import org.example.pokerbakend.services.models.Card;
import org.example.pokerbakend.services.models.Dealer;
import org.example.pokerbakend.services.models.Player;
import org.example.pokerbakend.services.models.messages.ActionMessage;
import org.example.pokerbakend.services.models.messages.join.JoinResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

//To jest model z MVC
@Service
public class GameService {
    private final SimpMessagingTemplate messagingTemplate;
    private final SecurityService securityService;
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

    public List<Player> getAllPlayers() {
        return players;
    }

    public JoinResponse joinGame(String username) {
        if (players.size() < MAX_PLAYERS && !running) {
            String token = securityService.generateToken();
            int id = securityService.generateId();

            Player player = new Player(
                    id,
                    username,
                    token,
                    10_000
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
                    "Couldn't connect",
                    null,
                    null
            );
        }
    }

    public void leaveGame(Player player) {
        for (int i=0; i<players.size(); ++i) {
            if (players.get(i).getId().equals(player.getId())) {
                players.remove(i);
                System.out.println("Player " + player.getId() + " left the game");
                break;
            }
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

    private void dealCardsToPlayers(){
        for (Player player : players) {
            player.setHand(dealer.dealCards(2));
        }
    }

    private void dealCardsToTable(){
        if (tableCards.isEmpty()) {
            tableCards.addAll(dealer.dealCards(3));
        }
        else if (tableCards.size() == 3 || tableCards.size() == 4) {
            tableCards.addAll(dealer.dealCards(1));
        }
    }

    private void awaitPlayersMove(){
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


//    @SendTo("/topic/checkYourHand")
    private void round(){
        for (Player player : players) {
            currentPlayer = player;
//            SendToAll czyj jest teraz ruch

            messagingTemplate.convertAndSend("/topic/move", player.getId());
            awaitPlayersMove();
        }
    }

    public void action(ActionMessage message) {
        if (currentPlayer.getToken().equals(message.getToken())) {
            String action = message.getAction();
            switch (action) {
                case "fold": {
                    currentPlayer.fold();
                }
                case "raise": {
                    currentPlayer.raise(message.getAmount());
                    currentBet = message.getAmount();
                }
                case "check": {
                    currentPlayer.check();
                }
                case "call": {
                    currentPlayer.call(currentBet);
                }
            }

            awaitingMove = false;
        }
    }

    private void mainLoop(){
        while (running) {
//            Rozdanie kart
            dealCardsToPlayers();
            messagingTemplate.convertAndSend("/topic/checkYourHand","");

//            Wylozenie kart na stol
            dealCardsToTable();
            messagingTemplate.convertAndSend("/topic/tableCards", tableCards);

//            Pierwsza runda
            round();

        }
    }


}