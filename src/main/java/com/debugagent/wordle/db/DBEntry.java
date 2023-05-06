package com.debugagent.wordle.db;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public record DBEntry(String user, String guess, String word, int attempt) {
}

