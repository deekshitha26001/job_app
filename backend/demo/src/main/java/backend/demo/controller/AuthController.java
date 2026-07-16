package backend.demo.controller;

import backend.demo.dto.AuthRequest;
import backend.demo.dto.AuthResponse;
import backend.demo.dto.UserDto;
import backend.demo.entity.User;
import backend.demo.repository.UserRepository;
import backend.demo.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody AuthRequest request) {
        try {
            if (userRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Email already in use"));
            }

            User user = User.builder()
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .build();

            userRepository.save(user);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "User created successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "An error occurred"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
            String jwtToken = jwtService.generateToken(user);

            return ResponseEntity.ok(AuthResponse.builder().token(jwtToken).build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid credentials"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            User dbUser = userRepository.findById(user.getId()).orElseThrow();

            UserDto userDto = UserDto.builder()
                    .id(dbUser.getId())
                    .email(dbUser.getEmail())
                    .resumeUrl(dbUser.getResumeUrl())
                    .resumeName(dbUser.getResumeName())
                    .role(dbUser.getRole().name())
                    .build();

            return ResponseEntity.ok(userDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not authenticated"));
        }
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateMe(@RequestBody UserDto userDto) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            User dbUser = userRepository.findById(user.getId()).orElseThrow();

            dbUser.setResumeUrl(userDto.getResumeUrl());
            dbUser.setResumeName(userDto.getResumeName());

            userRepository.save(dbUser);

            UserDto updatedDto = UserDto.builder()
                    .id(dbUser.getId())
                    .email(dbUser.getEmail())
                    .resumeUrl(dbUser.getResumeUrl())
                    .resumeName(dbUser.getResumeName())
                    .role(dbUser.getRole().name())
                    .build();

            return ResponseEntity.ok(updatedDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Could not update profile"));
        }
    }
}
