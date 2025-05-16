package org.example.pokerbakend.services.models.messages;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActionMessage {
    private String token;
    private String action;

//    Wykorzystywany jesli gracz podbija stawke
    private Integer amount;
}
