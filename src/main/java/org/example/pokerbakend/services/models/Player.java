package org.example.pokerbakend.services.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

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
        balance+=bet;
        bet=amount;
        balance-=bet;
    }

    public void call(int amount){
        raise(amount);
    }
}
