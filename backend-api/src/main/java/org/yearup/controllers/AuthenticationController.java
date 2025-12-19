package org.yearup.controllers;

import javax.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import org.yearup.models.Profile;
import org.yearup.data.ProfileDao;
import org.yearup.data.UserDao;
import org.yearup.models.authentication.LoginDto;
import org.yearup.models.authentication.LoginResponseDto;
import org.yearup.models.authentication.RegisterUserDto;
import org.yearup.models.User;
import org.yearup.security.jwt.JWTFilter;
import org.yearup.security.jwt.TokenProvider;

@RestController
@CrossOrigin

// permitAll() means ANYONE can access login & register
@PreAuthorize("permitAll()")
public class AuthenticationController
{
    // Responsible for creating JWT tokens
    private final TokenProvider tokenProvider;

    // Spring Security object that verifies username + password
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    // Used to read/write users from database
    private final UserDao userDao;

    // Used to create a blank profile when a user registers
    private final ProfileDao profileDao;

    // Constructor injection
    public AuthenticationController(
            TokenProvider tokenProvider,
            AuthenticationManagerBuilder authenticationManagerBuilder,
            UserDao userDao,
            ProfileDao profileDao)
    {
        this.tokenProvider = tokenProvider;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.userDao = userDao;
        this.profileDao = profileDao;
    }

    // ===========================
    // LOGIN
    // ===========================
    // POST /login
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<LoginResponseDto> login(
            @Valid @RequestBody LoginDto loginDto)
    {
        try
        {
            // Create a Spring Security authentication token
            // This DOES NOT hit the database yet
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(
                            loginDto.getUsername(),
                            loginDto.getPassword()
                    );

            // Authenticate username/password against UserDetailsService
            Authentication authentication =
                    authenticationManagerBuilder
                            .getObject()
                            .authenticate(authenticationToken);

            // Store authenticated user in Spring Security context
            // This is what makes Principal available later
            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);

            // Generate JWT token from authenticated user
            String jwt = tokenProvider.createToken(authentication, false);

            // Fetch full User object from database
            User user = userDao.getByUserName(loginDto.getUsername());
            if (user == null)
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);

            // Add token to HTTP response header
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add(
                    JWTFilter.AUTHORIZATION_HEADER,
                    "Bearer " + jwt
            );

            // Return token + user object in response body
            return new ResponseEntity<>(
                    new LoginResponseDto(jwt, user),
                    httpHeaders,
                    HttpStatus.OK
            );
        }
        catch (BadCredentialsException ex)
        {
            // Wrong username or password
            // IMPORTANT: must return 401 (not 500)
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid username or password."
            );
        }
        catch (ResponseStatusException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            // Any unexpected error
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Oops... our bad."
            );
        }
    }


    // REGISTER
    // POST /register
    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ResponseEntity<User> register(
            @Valid @RequestBody RegisterUserDto newUser)
    {
        try
        {
            // Check if username already exists
            boolean exists = userDao.exists(newUser.getUsername());
            if (exists)
            {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "User Already Exists."
                );
            }

            // Create new user in users table
            User user = userDao.create(
                    new User(
                            0,
                            newUser.getUsername(),
                            newUser.getPassword(),
                            newUser.getRole()
                    )
            );

            // Create a blank profile row
            // This avoids 404 when /profile is requested
            Profile profile = new Profile();
            profile.setUserId(user.getId());
            profileDao.create(profile);

            // Return created user
            return new ResponseEntity<>(user, HttpStatus.CREATED);
        }
        catch (ResponseStatusException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Oops... our bad."
            );
        }
    }
}
