package org.example.pokerbakend.services.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Spectator extends User {
    public Spectator(Integer id) {
        super(id);
    }
}
