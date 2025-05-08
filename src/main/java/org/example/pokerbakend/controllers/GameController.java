package org.example.pokerbakend.controllers;



import org.example.pokerbakend.models.Card;
import org.example.pokerbakend.models.Player;
import org.example.pokerbakend.models.messages.BroadcastMessage;
import org.example.pokerbakend.models.messages.SuccessMessage;
import org.example.pokerbakend.models.messages.TokenMessage;
import org.example.pokerbakend.models.messages.join.JoinMessage;
import org.example.pokerbakend.models.messages.PrivateMessage;
import org.example.pokerbakend.models.messages.join.JoinResponse;
import org.example.pokerbakend.models.messages.spectate.SpectateMessage;
import org.example.pokerbakend.models.messages.spectate.SpectateResponse;
import org.example.pokerbakend.services.GameService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.util.List;


@Controller
public class GameController {
    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

//    User input
//    Broadcast messages
    @MessageMapping("/sendBroadcast")
    @SendTo("/topic/broadcasts")
    public String broadcastMessage(BroadcastMessage message){
        return message.getMessage();
    }


//    Private messages
    @MessageMapping("/privateMessage")
    @SendToUser("/queue/private")
    public String privateMessage(PrivateMessage message){
        return message.getMessage();
    }


    @MessageMapping("/join")
    @SendToUser("/queue/join/response")
    public JoinResponse joinGame(JoinMessage joinMessage){
        return gameService.joinGame(joinMessage.getUsername());
    }


    @MessageMapping("/spectate")
    @SendToUser("/queue/spectate/response")
    public SpectateResponse spectateGame(SpectateMessage spectateMessage){
        return gameService.spectateGame(spectateMessage.getUsername());
    }


    @MessageMapping("/getAll")
    @SendToUser("/queue/getAll/response")
    public List<Player> getAll(){
        return gameService.getAllPlayers();
    }

    @MessageMapping("/getHand")
    @SendToUser("/queue/getHand/response")
    public List<Card> getHand(TokenMessage tokenMessage){
        return gameService.getHand(tokenMessage.getToken());
    }


    @MessageMapping("/leave")
    public void leaveGame(Player player){
        System.out.println(player);
        gameService.leaveGame(player);
    }

    @MessageMapping("/start")
    @SendTo("/topic/checkYourHand")
    public SuccessMessage startGame(Player player){
        return gameService.startGame(player);
    }
}
