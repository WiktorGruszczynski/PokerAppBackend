package org.example.pokerbakend.services;

import org.example.pokerbakend.services.models.Card;
import org.example.pokerbakend.services.models.HandEvaluation;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//             Poker hands
//            "High card"
//            "Pair"
//            "Two pair"
//            "Three of a kind"
//            "Four of a kind"
//            "Full house"
//            "Straight"
//            "Flush"
//            "Straight flush"

@Service
public class PokerHandService {
    private final List<String> possibleColors = List.of(
            "clubs",
            "diamonds",
            "hearts",
            "spades"
    );

    private List<Card> sort(List<Card> cards){
        for (int i=0; i<cards.size(); i++){
            for (int j=i+1; j<cards.size(); j++){
                if (cards.get(i).getIndex() > cards.get(j).getIndex()){
                    Card temp = cards.get(i);
                    cards.set(i, cards.get(j));
                    cards.set(j, temp);
                }
            }
        }

        return cards;
    }

    @SafeVarargs
    private List<Card> combineLists(List<Card>... cardsLists) {
        List<Card> cards = new ArrayList<>();

        for (List<Card> cardList : cardsLists) {
            cards.addAll(cardList);
        }

        return cards;
    }

    private List<Card> subtractList(List<Card> list, List<Card> listToSubtract){
        List<Card> result = new ArrayList<>();

        for (Card card: list){
            if (!listToSubtract.contains(card)) result.add(card);
        }

        return result;
    }

    private List<Card> removeDuplicateIndexesCards(List<Card> cards){
        List<Integer> foundIndexes = new ArrayList<>();
        List<Card> uniqueCards = new ArrayList<>();

        for (Card card : cards) {
            int index = card.getIndex();

            if (!foundIndexes.contains(index)){
                uniqueCards.add(card);
                foundIndexes.add(index);
            }
        }

        return uniqueCards;
    }

    private HashMap<Integer, List<Card>> mapCardsByIndexes(List<Card> cards){
        HashMap<Integer, List<Card>> indexesCounter = new HashMap<>();

        for (Card card : cards) {
            int index = card.getIndex();
            if (!indexesCounter.containsKey(index)) {
                indexesCounter.put(index, new ArrayList<>());
            }

            indexesCounter.get(index).add(card);
        }

        return indexesCounter;
    }

    public HandEvaluation evaluateHand(List<Card> playerCards, List<Card> tableCards){
        List<Card> cards = sort(combineLists(playerCards, tableCards));
        HandEvaluation bestHand = getBestHand(cards);
        setHandHighCard(bestHand, playerCards);

        return bestHand;
    }

    private HandEvaluation getBestHand(List<Card> cards){
        HandEvaluation hand;

        hand = checkStraightFlush(cards);
        if (hand!=null) return hand;

        hand = checkQuads(cards);
        if (hand!=null) return hand;

        hand = checkFullHouse(cards);
        if (hand!=null) return hand;

        hand = checkFlush(cards);
        if (hand!=null) return hand;

        hand = checkStraight(cards);
        if (hand!=null) return hand;

        hand = checkTrips(cards);
        if (hand!=null) return hand;

        hand = checkPairs(cards);
        if (hand!=null) return hand;

        return new HandEvaluation(null, new ArrayList<>());
    }

    private void setHandHighCard(HandEvaluation hand, List<Card> playerCards){
        if (hand.getHandTitle()!=null) playerCards = subtractList(playerCards, hand.getCards());
        Card highCard = playerCards.get(0);

        for (Card card: playerCards){
            if (card.getIndex()>highCard.getIndex()) highCard=card;
        }

        hand.setHighCard(highCard);
    }

    private HandEvaluation checkPairs(List<Card> cards){
        HashMap<Integer, List<Card>> indexesCounter = mapCardsByIndexes(cards);
        List<Card> pairs = new ArrayList<>();

        for (int key: indexesCounter.keySet()){
            List<Card> sameIndexCards = indexesCounter.get(key);

            if (sameIndexCards.size()==2){
                pairs.addAll(sameIndexCards);
            }
        }

        if (pairs.size()==2){
            return new HandEvaluation(
                    "Pair",
                    pairs
            );
        }
        if (pairs.size()==4){
            return new HandEvaluation(
                    "Two pair",
                    pairs
            );
        }
        return null;
    }

    private HandEvaluation checkTrips(List<Card> cards){
        HashMap<Integer, List<Card>> indexesCounter = mapCardsByIndexes(cards);

        for (int key: indexesCounter.keySet()){
            List<Card> sameIndexCards = indexesCounter.get(key);

            if (sameIndexCards.size()==3){
                return new HandEvaluation(
                    "Three of a kind", sameIndexCards
                );
            }
        }

        return null;
    }

    private HandEvaluation checkQuads(List<Card> cards){
        HashMap<Integer, List<Card>> indexesCounter = mapCardsByIndexes(cards);

        for (int key: indexesCounter.keySet()){
            List<Card> sameIndexCards = indexesCounter.get(key);
            if (sameIndexCards.size()==4){
                return new HandEvaluation(
                        "Four of a kind", sameIndexCards
                );
            }
        }

        return null;
    }

    private HandEvaluation checkFullHouse(List<Card> cards){
        HandEvaluation pairsEval = checkPairs(cards);
        HandEvaluation tripsEval = checkTrips(cards);

        if (pairsEval!=null && tripsEval!=null){
            List<Card> bestPair = pairsEval.getCards().subList(
                    pairsEval.getCards().size()-2,
                    pairsEval.getCards().size()
            );

            return new HandEvaluation(
                    "Full house", combineLists(bestPair, tripsEval.getCards())
            );
        }
        return null;
    }

    private HandEvaluation checkFlush(List<Card> cards){
        for (String color : possibleColors) {
            List<Card> fittingCards = new ArrayList<>();

            for (Card card : cards) {
                if (card.getColor().equals(color)) fittingCards.add(card);
            }

            if (fittingCards.size() == 5){
                return new HandEvaluation(
                        "Flush",
                        fittingCards
                );
            }
        }
        return null;
    }

    private HandEvaluation checkStraight(List<Card> cards){
        List<Card> preparedCards = removeDuplicateIndexesCards(cards);

        if (preparedCards.size()>=5){
            HandEvaluation eval = null;

//            sprawdz wszystkie piątki
            for (int i=0; i<=preparedCards.size()-5; i++){
                int maxDiff = 0;
                List<Card> checkedCards = new ArrayList<>();

                for (int j=0; j<5-1; j++){
                    Card firstCard = preparedCards.get(j+i);
                    Card secondCard = preparedCards.get(j+i+1);

                    maxDiff = Math.max(
                            Math.abs(firstCard.getIndex() - secondCard.getIndex()), maxDiff
                    );

                    checkedCards.add(firstCard);
                }
                checkedCards.add(preparedCards.get(i+4));

//                Jest straight
                if (maxDiff == 1){

                    if (eval==null){
                        eval = new HandEvaluation("Straight",checkedCards);
                    }
                    else{
                        HandEvaluation newEval = new HandEvaluation("Straight",checkedCards);

                        if (newEval.getPoints()> eval.getPoints()){
                            eval = newEval;
                        }
                    }
                }
            }

//            Check A, 2, 3, 4, 5
            if (eval==null){
                List<Card> filteredCards = new ArrayList<>();
                List<Integer> allowedIndexes = List.of(14,2,3,4,5);

                for (Card card : preparedCards){
                    if (allowedIndexes.contains(card.getIndex())){
                        filteredCards.add(card);
                    }
                }

                if (filteredCards.size()==5){
                    eval = new HandEvaluation("Straight",filteredCards);
                }
            }

            return eval;
        }

        return null;
    }

    private HandEvaluation checkStraightFlush(List<Card> cards){
        HandEvaluation flush = checkFlush(cards);
        if (flush!=null){
            HandEvaluation straightFlush = checkStraight(flush.getCards());

            if (straightFlush!=null){
                straightFlush.setHandTitle("Straight flush");
                return straightFlush;
            }
        }
        return null;
    }
}