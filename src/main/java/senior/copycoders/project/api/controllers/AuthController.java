package senior.copycoders.project.api.controllers;


import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import senior.copycoders.project.api.services.UserService;
import senior.copycoders.project.store.entities.PersonEntity;

import java.util.Objects;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private UserService service;

    public AuthController(UserService service) {
        this.service = service;
    }

    @PostMapping(path = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody PersonEntity getAuthUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null;
        }


        Object principal = auth.getPrincipal();
        User user = (principal instanceof User) ? (User) principal : null;

        if (user == null) {
            return null;
        } else {
            return this.service.getByUsername(user.getUsername()).orElse(null);
        }
    }

}
