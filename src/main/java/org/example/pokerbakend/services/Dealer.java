package org.example.pokerbakend.services;


import org.example.pokerbakend.services.models.Card;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Dealer {
    private final List<Card> backupCards;
    private List<Card> gameCards;
    private final Random rand = new Random();

    public Dealer(List<Card> cards) {
        this.backupCards = cards;
        this.gameCards = cards;
    }

    public List<Card> dealCards(int amount) {
        List<Card> cards = new ArrayList<>();

        for (int i = 0; i < amount; i++) {
            int randIndex = rand.nextInt(gameCards.size());
            cards.add(gameCards.get(randIndex));
            gameCards.remove(randIndex);
        }

        return cards;
    }

    public void resetDeck(){
        gameCards = backupCards;
    }
}
