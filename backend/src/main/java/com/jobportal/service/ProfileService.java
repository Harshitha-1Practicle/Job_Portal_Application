package com.jobportal.service;

import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.model.Profile;
import com.jobportal.model.User;
import com.jobportal.repository.ProfileRepository;
import com.jobportal.repository.UserRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    public ProfileService(ProfileRepository profileRepository, UserRepository userRepository) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    public Profile getProfile(@NonNull Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return profileRepository.findByUser(user)
                .orElseGet(() -> {
                    Profile profile = new Profile();
                    profile.setUser(user);
                    return profileRepository.save(profile);
                });
    }

    public Profile updateProfile(@NonNull Long userId, Profile updatedProfile) {
        Profile existing = getProfile(userId);
        existing.setEducation(updatedProfile.getEducation());
        existing.setSkills(updatedProfile.getSkills());
        existing.setExperience(updatedProfile.getExperience());
        existing.setResume(updatedProfile.getResume());
        existing.setProfileImage(updatedProfile.getProfileImage());
        return profileRepository.save(existing);
    }
}
