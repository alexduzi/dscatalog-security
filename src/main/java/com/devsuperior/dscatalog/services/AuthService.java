package com.devsuperior.dscatalog.services;

import com.devsuperior.dscatalog.dto.EmailDTO;
import com.devsuperior.dscatalog.entities.PasswordRecover;
import com.devsuperior.dscatalog.entities.User;
import com.devsuperior.dscatalog.repositories.PasswordRecoverRepository;
import com.devsuperior.dscatalog.repositories.UserRepository;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
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

    public AuthService(EmailService emailService, UserRepository userRepository, PasswordRecoverRepository passwordRecoverRepository) {
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.passwordRecoverRepository = passwordRecoverRepository;
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
}
