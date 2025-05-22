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

    private HandEvaluation handEvaluation;

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

    public void addBalance(int amount){
        this.balance += amount;
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

        if (balance-amount < 0){
//            jezeli bet większy niz balance to wchodzi caly balance

            bet = balance;
            balance = 0;
        }
        else    if (amount<tableBet){
            throw new IllegalMoveException("Bet is too small!");
        }
        else {
            balance-=amount;
            bet=amount;
        }
    }

    public void check(int tableBet){
        checkStatus();

        if (bet<tableBet && balance-tableBet >= 0){
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
