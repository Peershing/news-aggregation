package com.fracta.newsaggregation.service;

import java.time.Instant;
import java.util.UUID;

import javax.transaction.Transactional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Service;

import com.fracta.newsaggregation.dto.AuthenticationResponse;
import com.fracta.newsaggregation.dto.LoginRequest;
import com.fracta.newsaggregation.dto.RegisterRequest;
import com.fracta.newsaggregation.exception.NewsAggregationException;
import com.fracta.newsaggregation.model.NotificationEmail;
import com.fracta.newsaggregation.model.User;
import com.fracta.newsaggregation.model.VerificationToken;
import com.fracta.newsaggregation.repo.UserRepo;
import com.fracta.newsaggregation.repo.VerificationTokenRepo;
import com.fracta.newsaggregation.security.JwtProvider;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AuthService {
	
	private final UserRepo userRepo;
	private final PasswordEncoder passwordEncoder;
	private final VerificationTokenRepo verificationTokenRepo;
	private final MailService mailService;
	private final AuthenticationManager authenticationManager;
	private final JwtProvider jwtProvider;
	
	@Transactional
	public void signup(RegisterRequest registerRequest) {
		var user = new User();
		user.setUsername(registerRequest.getUsername());
		user.setEmail(registerRequest.getEmail());
		user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
		user.setCreationDate(Instant.now());
		
		userRepo.save(user);
		var verificationToken = generateVerificationToken(user);
		
		mailService.sendEmail(new NotificationEmail(user.getEmail(), "Account Activation",
				String.format("Hello, %s!\n\nWhat can we say? You're part of us from now. Welcome to Fractaline.\n\n"
				+ "Activate your account: http://localhost:8080/api/auth/accountVerification/%s",
				registerRequest.getUsername().toString(), verificationToken.getName())));
	}

	private VerificationToken generateVerificationToken(User user) {
		var token = UUID.randomUUID().toString();
		var verificationToken = new VerificationToken();
		verificationToken.setName(token);
		verificationToken.setUser(user);
		
		verificationTokenRepo.save(verificationToken);
		return verificationToken;
	}

	public void verifyUser(String tokenName) {
		var verificationToken = verificationTokenRepo.findByName(tokenName)
			.orElseThrow(() -> new NewsAggregationException(String.format("$s token is invalid.", tokenName)));
		fetchUserAndEnable(verificationToken);
	}
	
	@Transactional
	private void fetchUserAndEnable(VerificationToken verificationToken) {
		var user = userRepo.findByUserId(verificationToken.getUser().getUserId())
				.orElseThrow(() -> new NewsAggregationException("User not found."));
		user.setEnabled(true);
		userRepo.save(user);
	}

	public AuthenticationResponse login(LoginRequest loginRequest) {
		var authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
				loginRequest.getUsername(), loginRequest.getPassword()));
		
		SecurityContextHolder.getContext().setAuthentication(authentication);
		var token = jwtProvider.generateToken(authentication);
		return new AuthenticationResponse(token, loginRequest.getUsername());
	}
}