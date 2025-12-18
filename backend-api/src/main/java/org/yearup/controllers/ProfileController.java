package org.yearup.controllers;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.yearup.data.ProfileDao;
import org.yearup.models.Profile;
import org.yearup.data.UserDao;
import org.yearup.models.User;
@CrossOrigin
@RestController
@RequestMapping("/profile")
@PreAuthorize("isAuthenticated")

public class ProfileController {
    private final ProfileDao profileDao;
    private final UserDao userDao;

    public ProfileController(ProfileDao profileDao, UserDao userDao) {
        this.profileDao = profileDao;
        this.userDao = userDao;
    }
    @GetMapping
    public  ResponseEntity<Profile> getProfile(Authentication authentication) {
        String username = authentication.getName();
        int userId = userDao.getIdByUsername(username);
        Profile profile = profileDao.getByUserId(userId);

        if (profile == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(profile);
    }
    @PutMapping
    public ResponseEntity<Profile> updateProfile(Authentication authentication, @RequestBody Profile profile)
    {
        String username = authentication.getName();
        int userId = userDao.getIdByUsername(username);
        profile.setUserId(userId);
        profileDao.update(profile);

        Profile updated = profileDao.getByUserId(userId);
        return ResponseEntity.ok(updated);

    }
}


