package com.cmps.ims;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordCheckTest {

    @Test
    void checkPassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        boolean matches = encoder.matches("password", "$2a$10$spN1GzFZ4SqYP7GRzVCuZ.SOQ4H/JOJO/MLOXoKiWGoiKp57BDfyC");
        System.out.println("MATCH RESULT: " + matches);
    }
    
    @Test
    void generateHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.encode("password"));
    }
}