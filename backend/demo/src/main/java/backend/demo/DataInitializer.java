package backend.demo;

import backend.demo.entity.Role;
import backend.demo.entity.User;
import backend.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (!userRepository.existsByEmail("admin@job.com")) {
            User admin = User.builder()
                    .email("admin@job.com")
                    .password(passwordEncoder.encode("admin@98765"))
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(admin);
            log.info("Admin user created: admin@job.com");
        }
    }
}
