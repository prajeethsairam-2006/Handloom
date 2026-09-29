package com.handloom.marketplace.service;

import com.handloom.marketplace.model.Artisan;
import com.handloom.marketplace.model.User;
import com.handloom.marketplace.repository.ArtisanRepository;
import com.handloom.marketplace.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final ArtisanRepository artisanRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, ArtisanRepository artisanRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.artisanRepository = artisanRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No user found with email: " + email));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
    }

    @Transactional
    public User registerUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("An account with email " + user.getEmail() + " already exists.");
        }

        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Long userId = userRepository.save(user);
        user.setId(userId);

        // If registered as ARTISAN, create Artisan profile entry
        if ("ARTISAN".equalsIgnoreCase(user.getRole())) {
            Artisan artisan = new Artisan();
            artisan.setUserId(userId);
            artisan.setBusinessName(user.getBusinessName() != null && !user.getBusinessName().isEmpty() ?
                    user.getBusinessName() : user.getName() + " Creations");
            artisan.setCraftType(user.getCraftType() != null && !user.getCraftType().isEmpty() ?
                    user.getCraftType() : "Handicrafts");
            artisan.setLocation(user.getLocation() != null && !user.getLocation().isEmpty() ?
                    user.getLocation() : "India");
            artisan.setDescription(user.getDescription());
            artisan.setStatus("ACTIVE");
            artisanRepository.save(artisan);
        }

        return user;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public List<User> findAllCustomers() {
        return userRepository.findAllByRole("CUSTOMER");
    }

    public int countCustomers() {
        return userRepository.countByRole("CUSTOMER");
    }
}
