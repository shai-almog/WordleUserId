package com.debugagent.wordle.service;

import com.debugagent.wordle.db.DBEntry;
import com.debugagent.wordle.db.Database;
import com.debugagent.wordle.webservices.CharacterResult;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Service
@RequiredArgsConstructor
public class WordleService {
    private static final String WORD = "LYMPH";
    private static final List<String> DICTIONARY = new ArrayList<>();

    private final Database database;

    static {
        try(InputStream is = new ClassPathResource("words.txt").getInputStream()) {
            Scanner scanner = new Scanner(is).useDelimiter("\n");
            while(scanner.hasNext()) {
                DICTIONARY.add(scanner.next().toUpperCase());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String validate(String word) {
        if(word.length() != 5) {
            return "Bad Length!";
        }
        word = word.toUpperCase();
        if(!DICTIONARY.contains(word)) {
            return "Not a word!";
        }
        return null;
    }

    public CharacterResult[] calculateResults(String userId, String word) {
        word = word.toUpperCase();
        database.insert(new DBEntry(userId, word, WORD, 0));

        CharacterResult[] result = new CharacterResult[WORD.length()];
        for(int iter = 0 ; iter < word.length() ; iter++) {
            char currentChar = word.charAt(iter);
            if(currentChar == WORD.charAt(iter)) {
                result[iter] = CharacterResult.GREEN;
                continue;
            }
            if(WORD.indexOf(currentChar) > -1) {
                result[iter] = CharacterResult.YELLOW;
                continue;
            }
            result[iter] = CharacterResult.BLACK;
        }
        return result;
    }
}
