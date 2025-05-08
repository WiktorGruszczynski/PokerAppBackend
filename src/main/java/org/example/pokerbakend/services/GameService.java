package org.example.pokerbakend.services;



import org.example.pokerbakend.models.Card;
import org.example.pokerbakend.models.Dealer;
import org.example.pokerbakend.models.Player;
import org.example.pokerbakend.models.Spectator;
import org.example.pokerbakend.models.messages.SuccessMessage;
import org.example.pokerbakend.models.messages.join.JoinResponse;
import org.example.pokerbakend.models.messages.spectate.SpectateResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class GameService {
    private final SecurityService securityService;

    private final Random rand = new Random();
    private final List<Player> players = new ArrayList<>();
    private final List<Spectator> spectators = new ArrayList<>();
    private final Dealer dealer;
    private final int MAX_PLAYERS = 5;

    private boolean running = false;
    private Player currentPlayer;

    public GameService(JsonReaderService jsonReaderService, SecurityService securityService) {
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


    public SpectateResponse spectateGame(String username) {
        Spectator spectator = new Spectator(
                new Random().nextInt(100_000, 999_999),
                username
        );

        spectators.add(spectator);

        return new SpectateResponse(
                true,
                "connected succesfully",
                spectator
        );
    }


    private boolean isValidPlayer(Player player) {
        for (Player p : players) {
            if (p.getId().equals(player.getId())) {
                return true;
            }
        }
        return false;
    }


    public SuccessMessage startGame(Player player) {
        if (isValidPlayer(player)) {
            if (players.size() < MAX_PLAYERS && players.size() > 1) {
                running = true;

//                deal cards for each player
                for (Player p : players) {
                    p.setHand(dealer.dealCards(2));
                }
            }
            return new SuccessMessage(true);
        }
        else{
            System.out.println("Not a valid player");
            return new SuccessMessage(false);
        }
    }

    public List<Card> getHand(String token) {
        for (Player player: players){
            if (player.getToken().equals(token)) {
                return player.getHand();
            }
        }
        return null;
    }
}
