package org.example.pokerbakend.services.models.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.pokerbakend.services.models.Card;
import org.example.pokerbakend.services.models.Player;
import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateMessage {
    private Player currentPlayer;
    private List<Card> table;
    private List<Player> players;
    private String message;
}
