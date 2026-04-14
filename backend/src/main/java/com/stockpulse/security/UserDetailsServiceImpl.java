package com.stockpulse.security;

import com.stockpulse.model.User;
import com.stockpulse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * UserDetailsServiceImpl
 * Loads user data from MySQL for Spring Security authentication.
 * Called by JwtAuthFilter on every protected request.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() ->
                    new UsernameNotFoundException("User not found with email: " + email)
                );

        // Build Spring Security UserDetails with role
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}