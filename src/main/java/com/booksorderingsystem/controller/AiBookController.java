package com.booksorderingsystem.controller;

import com.booksorderingsystem.dto.ai.AiBookRequest;
import com.booksorderingsystem.dto.ai.AiBookResponse;
import com.booksorderingsystem.dto.user.LoginResponse;
import com.booksorderingsystem.service.AiBookService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/ai/books")
@RequiredArgsConstructor
public class AiBookController {
    private final AiBookService aiBookService;

    @PostMapping("/ask")
    public AiBookResponse ask(
            @Valid @RequestBody AiBookRequest request,
            HttpServletRequest httpRequest
    ) {
        checkLogin(httpRequest);
        String answer = aiBookService.ask(request.getQuestion());
        return new AiBookResponse(answer);
    }

    private void checkLogin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "請先登入");
        }

        Object loginUser = session.getAttribute(AuthController.LOGIN_USER_SESSION_KEY);

        if (!(loginUser instanceof LoginResponse)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "請先登入");
        }
    }
}
