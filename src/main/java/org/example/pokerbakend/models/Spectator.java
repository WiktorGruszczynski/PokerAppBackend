package org.example.pokerbakend.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Spectator extends User {
    public Spectator(Integer id, String name) {
        super(id, name);
    }
}
