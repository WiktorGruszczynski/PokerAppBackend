package org.example.pokerbakend;

import org.example.pokerbakend.services.PokerHandService;
import org.example.pokerbakend.services.models.Card;

import java.util.List;

public class Test {
    public static void main(String[] args) {
        List<Card> tableCards = List.of(
          new Card(1, 11, "", "spades"),
                new Card(1, 10, "", "spades"),
                new Card(1, 12, "", "spades"),
                new Card(1, 14, "", "hearts"),
                new Card(1, 9, "", "spades")
        );

        List<Card> playerCards = List.of(
                new Card(1, 14, "", "clubs"),
                new Card(1, 8, "", "spades")
        );

        PokerHandService pokerHandService = new PokerHandService();

        System.out.println(pokerHandService.evaluateHand(playerCards, tableCards));

    }
}
