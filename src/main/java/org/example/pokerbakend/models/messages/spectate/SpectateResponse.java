package org.example.pokerbakend.models.messages.spectate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.pokerbakend.models.Spectator;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SpectateResponse {
    private boolean success;
    private String message;
    private Spectator spectator;
}
