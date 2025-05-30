package org.example.pokerbakend.services.models.messages;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActionMessage extends TokenMessage {
    private String action;
//    Wykorzystywany jesli gracz podbija stawke
    private Integer amount;
}
