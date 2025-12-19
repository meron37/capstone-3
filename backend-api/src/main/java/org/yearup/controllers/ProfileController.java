package org.yearup.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import org.yearup.data.ProfileDao;
import org.yearup.data.UserDao;
import org.yearup.models.Profile;

@CrossOrigin
@RestController
@RequestMapping("/profile")

// This controller can ONLY be accessed by authenticated (logged-in) users
// The JWT token is required for all endpoints in this controller
@PreAuthorize("isAuthenticated()")
public class ProfileController
{
    // DAO used to read/update profile data from the database
    private final ProfileDao profileDao;

    // DAO used to translate username → userId
    private final UserDao userDao;

    // Constructor injection (Spring provides the DAOs automatically)
    public ProfileController(ProfileDao profileDao, UserDao userDao)
    {
        this.profileDao = profileDao;
        this.userDao = userDao;
    }


    // GET /profile

    // Returns the profile of the currently logged-in user
    @GetMapping
    public ResponseEntity<Profile> getProfile(Authentication authentication)
    {
        // Authentication comes from the JWT token
        // The username was stored inside the token at login time
        String username = authentication.getName();

        // Convert username → userId (database uses user_id)
        int userId = userDao.getIdByUsername(username);

        // Fetch the user's profile from the database
        Profile profile = profileDao.getByUserId(userId);

        // If no profile exists, return 404 Not Found
        if (profile == null)
        {
            return ResponseEntity.notFound().build();
        }

        // Otherwise, return the profile with HTTP 200 OK
        return ResponseEntity.ok(profile);
    }


    // PUT /profile

    // Updates the profile of the currently logged-in user
    @PutMapping
    public ResponseEntity<Profile> updateProfile(
            Authentication authentication,
            @RequestBody Profile profile)
    {
        // Get the username from the JWT token
        String username = authentication.getName();

        // Convert username → userId
        int userId = userDao.getIdByUsername(username);

        // IMPORTANT:
        // Force the profile to belong to the logged-in user
        // This prevents users from updating someone else's profile
        profile.setUserId(userId);

        // Update the profile in the database
        profileDao.update(profile);

        // Fetch the updated profile so we can return it
        Profile updated = profileDao.getByUserId(userId);

        // Return updated profile with HTTP 200 OK
        return ResponseEntity.ok(updated);
    }
}
