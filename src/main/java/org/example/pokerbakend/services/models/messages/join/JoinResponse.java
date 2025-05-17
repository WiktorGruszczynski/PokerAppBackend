package org.example.pokerbakend.services.models.messages.join;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.pokerbakend.services.models.Player;
import org.example.pokerbakend.services.models.messages.TokenMessage;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JoinResponse extends TokenMessage {
    private boolean success;
    private String message;
    private Player player;

    public JoinResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public JoinResponse(boolean success, String message, String token, Player player) {
        this.success = success;
        this.message = message;
        this.player = player;
        this.setToken(token);
    }
}
