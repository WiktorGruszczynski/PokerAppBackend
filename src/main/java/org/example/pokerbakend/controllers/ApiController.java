package org.example.pokerbakend.controllers;



import org.example.pokerbakend.services.models.Card;
import org.example.pokerbakend.services.models.Player;
import org.example.pokerbakend.services.models.messages.ActionMessage;
import org.example.pokerbakend.services.models.messages.BroadcastMessage;
import org.example.pokerbakend.services.models.messages.PrivateMessage;
import org.example.pokerbakend.services.models.messages.TokenMessage;
import org.example.pokerbakend.services.models.messages.join.JoinMessage;
import org.example.pokerbakend.services.models.messages.join.JoinResponse;
import org.example.pokerbakend.services.GameService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.util.List;


@Controller
public class ApiController {
    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;

    public ApiController(GameService gameService, SimpMessagingTemplate messagingTemplate) {
        this.gameService = gameService;
        this.messagingTemplate = messagingTemplate;
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
        gameService.leaveGame(player);
    }

    @MessageMapping("/start")
    public void startGame(Player player){
        gameService.startGame(player);
    }

    @MessageMapping("/action")
    public void playerAction(ActionMessage message){
        gameService.action(message);
    }
}
