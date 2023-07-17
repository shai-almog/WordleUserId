package com.debugagent.wordle.web;

import com.debugagent.wordle.service.WordleService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@Scope("session")
@RequiredArgsConstructor
public class WebController {
    private final WordleService wordleService;

    private List<String> attempts = new ArrayList<>();

    @GetMapping("/guessMVC")
    public String guess(HttpServletResponse response,
                        @CookieValue(required = false) String userId,
                        Model model) {
        userId = initUserId(response, userId);
        showAttempts(userId, model);
        return "WordleGuess";
    }

    private String initUserId(HttpServletResponse response,
    String userId) {
        if(userId == null) {
            userId = UUID.randomUUID().toString();
            Cookie cookie = new Cookie("userId", userId);

            // cookie should expire in 10 years
            cookie.setMaxAge(3650 * 24 * 60 * 60);
            response.addCookie(cookie);
        }
        return userId;
    }

    @PostMapping("/guessMVC")
    public String submitGuess(HttpServletResponse response,
                              @CookieValue(required = false) String userId,
                              String guess, Model model) {
        userId = initUserId(response, userId);
        String error = wordleService.validate(guess);
        if(error != null) {
            model.addAttribute("errorMessage", error);
        } else {
            model.addAttribute("errorMessage", "");
            attempts.add(guess);
        }
        showAttempts(userId, model);

        return "WordleGuess";
    }

    private void showAttempts(String userId, Model model) {
        List<WebResult[]> results = attempts.stream()
                .map(str -> WebResult.create(wordleService.calculateResults(userId, str), str))
                .collect(Collectors.toList());
        model.addAttribute("entries", results);
    }

}
