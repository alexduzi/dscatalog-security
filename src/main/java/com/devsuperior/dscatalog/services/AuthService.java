package com.devsuperior.dscatalog.services;

import com.devsuperior.dscatalog.dto.EmailDTO;
import com.devsuperior.dscatalog.dto.NewPasswordDTO;
import com.devsuperior.dscatalog.entities.PasswordRecover;
import com.devsuperior.dscatalog.entities.User;
import com.devsuperior.dscatalog.repositories.PasswordRecoverRepository;
import com.devsuperior.dscatalog.repositories.UserRepository;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    @Value("${email.password-recover.token.minutes}")
    private Long tokenMinutes;

    @Value("${email.password-recover.uri}")
    private String recoverUri;

    private final UserRepository userRepository;

    private final EmailService emailService;

    private final PasswordRecoverRepository passwordRecoverRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public AuthService(EmailService emailService, UserRepository userRepository, PasswordRecoverRepository passwordRecoverRepository, PasswordEncoder passwordEncoder) {
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.passwordRecoverRepository = passwordRecoverRepository;
        this.bCryptPasswordEncoder = (BCryptPasswordEncoder) passwordEncoder;
    }

    @Transactional
    public void createRecoveryToken(@Valid EmailDTO body) {
        User user = userRepository.findByEmail(body.getEmail()).orElseThrow(() -> new ResourceNotFoundException("Email não encontrado"));

        PasswordRecover passwordRecover = new PasswordRecover();
        passwordRecover.setEmail(body.getEmail());
        passwordRecover.setToken(UUID.randomUUID().toString());
        passwordRecover.setExpiration(Instant.now().plusSeconds(tokenMinutes * 60L));
        passwordRecover = passwordRecoverRepository.save(passwordRecover);

        String text = "Acesse o link para definir uma nova senha\n\n"
                      + recoverUri + passwordRecover.getToken();

        emailService.sendEmail(body.getEmail(), "Recuperacao de senha", text);
    }

    @Transactional
    public void saveNewPassword(@Valid NewPasswordDTO body) {
        List<PasswordRecover> result = passwordRecoverRepository.searchValidTokens(body.getToken(), Instant.now());

        if (result.isEmpty()) {
            throw new ResourceNotFoundException("Invalid token!");
        }

        User user = userRepository.findByEmail(result.get(0).getEmail()).orElseThrow(() -> new ResourceNotFoundException("User not found!"));

        user.setPassword(bCryptPasswordEncoder.encode(body.getPassword()));

        userRepository.save(user);
    }

    protected Optional<User> authenticated() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Jwt jwtPrincipal = (Jwt) authentication.getPrincipal();
            String username = jwtPrincipal.getClaim("username");
            return userRepository.findByEmail(username);
        } catch (Exception e) {
            throw new UsernameNotFoundException("Invalid user");
        }
    }
}
