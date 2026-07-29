package com.cmps.ims.security;

import com.cmps.ims.entity.User;
import com.cmps.ims.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        System.out.println("=== Login Attempt ID: [" + userId + "] ===");
        
        User user = userRepository.findByUserId(userId)
            .orElseThrow(() -> {
                System.out.println("ERROR: User ID '" + userId + "' not found in database!");
                return new UsernameNotFoundException("ユーザーが見つかりません: " + userId);
            });
            
        System.out.println("SUCCESS: User found in DB. Stored hash is: [" + user.getPassword() + "]");
        return new CustomUserDetails(user);
    }
}