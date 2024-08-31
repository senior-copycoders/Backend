package senior.copycoders.project.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import senior.copycoders.project.api.dto.*;
import senior.copycoders.project.api.exceptions.ErrorDto;
import senior.copycoders.project.api.services.AuthenticationService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация")
public class AuthController {
    private final AuthenticationService authenticationService;

    @Operation(summary = "Регистрация пользователя")
    @PostMapping("/sign-up")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful response.",
                    content = @Content(schema = @Schema(implementation = JwtAuthenticationResponse.class))),

            @ApiResponse(responseCode = "400", description = "Invalid information about user.",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))),

    })
    public JwtAuthenticationResponse signUp(@RequestBody @Valid SignUpRequest request) {
        return authenticationService.signUp(request);
    }

    @Operation(summary = "Авторизация пользователя")
    @PostMapping("/sign-in")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful response.",
                    content = @Content(schema = @Schema(implementation = JwtAuthenticationResponse.class))),

            @ApiResponse(responseCode = "400", description = "Invalid information about user or token.",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))),

    })
    public JwtAuthenticationResponse signIn(@RequestBody @Valid SignInRequest request) {
        return authenticationService.signIn(request);
    }
}

