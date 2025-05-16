package org.example.pokerbakend.services.models;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Card {
    private Integer id;
    private Integer index;
    private String name;
    private String color;
}
