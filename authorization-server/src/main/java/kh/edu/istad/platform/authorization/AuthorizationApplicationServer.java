package kh.edu.istad.platform.authorization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AuthorizationApplicationServer {

    public static void main(String[] args) {
        SpringApplication.run(
                AuthorizationApplicationServer.class,
                args
        );
    }
}
