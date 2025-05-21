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


    public void raise(int amount){
        if (status.equals("fold")){
            throw new IllegalMoveException("You can't raise your bet after you already folded");
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
        if (bet<tableBet){
            throw new IllegalMoveException("Player cannot check, while his bet is smaller than table bet");
        }

        this.status = "checked";
    }

    public void call(int amount){
        raise(amount);
        check(amount);
    }
}
