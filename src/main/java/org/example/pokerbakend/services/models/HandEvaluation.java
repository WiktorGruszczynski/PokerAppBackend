package org.example.pokerbakend.services.models;


import lombok.*;

import java.util.List;

@ToString
@Getter
@Setter
@AllArgsConstructor
public class HandEvaluation {
    private String handTitle;
    private List<Card> cards;
    private Card highCard;

    public HandEvaluation(String handTitle, List<Card> cards){
        this.handTitle = handTitle;
        this.cards = cards;
    }

    public int getPoints(){
        int sum = 0;
        for (Card card: cards){
            sum+=card.getIndex();
        }

        if (handTitle.equals("Straight")){
            boolean hasTwo = false;
            boolean hasAce = false;

            for (Card card: cards){
                if (card.getIndex()==2) hasTwo=true;
                if (card.getIndex()==14) hasAce=true;
            }

            if (hasTwo&&hasAce){
                sum-=13;
            }
        }

        return sum;
    }
}
