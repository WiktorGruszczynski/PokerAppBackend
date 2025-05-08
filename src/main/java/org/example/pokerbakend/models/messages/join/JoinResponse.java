package org.example.pokerbakend.models.messages.join;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.pokerbakend.models.Player;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JoinResponse {
    private boolean success;
    private String message;
    private String token;
    private Player player;
}
