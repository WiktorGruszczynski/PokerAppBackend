package org.example.pokerbakend.services;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class SecurityService {
    private final Random rand = new Random();
    private final int TOKEN_LENGTH = 32;
    private final String characters = "0123456789abcdef";
    private int last_id = 0;

    public int generateId(){
        last_id++;
        return last_id;
    }

    public String generateToken(){
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < TOKEN_LENGTH; i++) {
            sb.append(
                    characters.charAt(rand.nextInt(characters.length()))
            );
        }

        return sb.toString();
    }
}
