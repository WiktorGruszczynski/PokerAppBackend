package org.example.pokerbakend;

import org.example.pokerbakend.services.PokerHandService;
import org.example.pokerbakend.services.models.Card;
import org.example.pokerbakend.services.models.HandEvaluation;

import java.util.List;

public class Test {
    public static void main(String[] args) {
        List<Card> tableCards = List.of(
          new Card(1, 11, "", "spades"),
                new Card(1, 14, "", "diamonds"),
                new Card(1, 12, "", "hearts"),
                new Card(1, 14, "", "hearts"),
                new Card(1, 9, "", "spades")
        );

        List<Card> player1Cards = List.of(
                new Card(1, 4, "", "clubs"),
                new Card(1, 14, "", "spades")
        );

        List<Card> player2Cards = List.of(
                new Card(1, 14, "", "clubs"),
                new Card(1, 4, "", "hearts")
        );

        PokerHandService pokerHandService = new PokerHandService();

//        HandEvaluation hand1 = pokerHandService.evaluateHand(player1Cards, tableCards);
//        HandEvaluation hand2 = pokerHandService.evaluateHand(player2Cards, tableCards);



    }
}
