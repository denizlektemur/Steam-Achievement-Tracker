package com.denizlektemur.steamachievementtracker.service;

import com.denizlektemur.steamachievementtracker.model.User;
import com.denizlektemur.steamachievementtracker.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    public User getUserBySteamId(String steamId) {
        return userRepository.findBySteamId(steamId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with steamId: " + steamId));
    }

    public User createUser(User user) {
        if (userRepository.existsBySteamId(user.getSteamId())) {
            throw new IllegalArgumentException("User already exists with steamId: " + user.getSteamId());
        }
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
}