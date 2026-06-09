package com.denizlektemur.steamachievementtracker.service;

import com.denizlektemur.steamachievementtracker.exception.DuplicateResourceException;
import com.denizlektemur.steamachievementtracker.exception.ResourceNotFoundException;
import com.denizlektemur.steamachievementtracker.model.User;
import com.denizlektemur.steamachievementtracker.repository.UserRepository;
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
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public User getUserBySteamId(String steamId) {
        return userRepository.findBySteamId(steamId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with steamId: " + steamId));
    }

    public User createUser(User user) {
        if (userRepository.existsBySteamId(user.getSteamId())) {
            throw new DuplicateResourceException("User already exists with steamId: " + user.getSteamId());
        }
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
}