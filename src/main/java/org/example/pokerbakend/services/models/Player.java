package org.example.pokerbakend.services.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.example.pokerbakend.services.exceptions.IllegalMoveException;

import java.util.List;

@Getter
@Setter
@ToString
public class Player extends User {
    @JsonIgnore
    private List<Card> hand;

    @JsonIgnore
    private String token;

    private Integer balance;
    private Integer bet;
    private Integer points;
    private String status = "active";

    public Player(Integer id, String name, String token, Integer balance) {
        super(id, name);
        this.token = token;
        this.balance = balance;
        this.bet = 0;
    }

    public void fold(){
        this.status = "fold";
    }

    private void checkStatus(){
        if (status.equals("fold")){
            throw new IllegalMoveException("Player folded!");
        }
    }

    public void raise(int amount, int tableBet){
        checkStatus();

        if (amount<tableBet){
            throw new IllegalMoveException("Bet is too small!");
        }

        balance+=bet;
        bet=amount;

//        if not enough funds -> wejdz All In
        if (balance<amount){
            bet = balance;
            balance = balance-amount;
        }
        else {
            balance-=bet;
        }
    }

    public void check(int tableBet){
        checkStatus();

        if (bet<tableBet){
            throw new IllegalMoveException("Player cannot check, while his bet is smaller than table bet");
        }
        if (tableBet==0){
            throw new IllegalMoveException("Player cannot check, if table bet is zero");
        }

        this.status = "checked";
    }

    public void call(int amount){
        checkStatus();
        raise(amount, amount);
        check(amount);
    }
}
