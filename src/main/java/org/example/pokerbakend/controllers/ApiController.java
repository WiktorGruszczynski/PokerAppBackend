package org.example.pokerbakend.controllers;



import org.example.pokerbakend.services.models.Card;
import org.example.pokerbakend.services.models.Player;
import org.example.pokerbakend.services.models.Spectator;
import org.example.pokerbakend.services.models.messages.ActionMessage;
import org.example.pokerbakend.services.models.messages.TokenMessage;
import org.example.pokerbakend.services.models.messages.join.JoinMessage;
import org.example.pokerbakend.services.models.messages.join.JoinResponse;
import org.example.pokerbakend.services.GameService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.util.List;


@Controller
public class ApiController {
    private final GameService gameService;

    public ApiController(GameService gameService) {
        this.gameService = gameService;
    }

    @MessageMapping("/join")
    @SendToUser("/queue/join/response")
    public JoinResponse joinGame(JoinMessage joinMessage){
        return gameService.joinGame(joinMessage.getUsername());
    }

    @MessageMapping("/start")
    public void startGame(Player player){
        gameService.startGame(player);
    }

    @MessageMapping("/getHand")
    @SendToUser("/queue/getHand/response")
    public List<Card> getHand(TokenMessage tokenMessage){
        return gameService.getHand(tokenMessage.getToken());
    }


    @MessageMapping("/action")
    public void playerAction(ActionMessage message){
        gameService.action(message);
    }

    @MessageMapping("/ready")
    public void setPlayerReady(TokenMessage tokenMessage){
        gameService.setPlayerReady(tokenMessage);
    }

    @MessageMapping("/isRunning")
    @SendToUser("/queue/isRunning/response")
    public boolean isGameRunning(){
        return gameService.isGameRunning();
    }

    @MessageMapping("/spectate")
    @SendToUser("/queue/spectate/response")
    public Spectator addSpectator(){
        return gameService.addSpectator();
    }

    @MessageMapping("fetchUpdate")
    public void fetchUpdate(){
        gameService.fetchUpdate();
    }
}
