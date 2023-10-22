package com.omaryan.logauth.appuser;
import java.time.LocalDateTime;
import java.util.UUID;

import com.omaryan.logauth.registration.token.ConfirmationToken;
import com.omaryan.logauth.registration.token.ConfirmationTokenService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AppUserService implements UserDetailsService {
    private final static String USER_NOT_FOUND = "user with email %s not found";
    private final AppUserRepositiory appUserRepositiory;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ConfirmationTokenService confirmationTokenService;
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return appUserRepositiory.findByEmail(email).orElseThrow(()->new UsernameNotFoundException(String.format(USER_NOT_FOUND,email)));
    }

    public String signUpUser(AppUser appUser){
        boolean userExists = appUserRepositiory.findByEmail(appUser.getEmail()).isPresent();
        if (userExists){
            throw new IllegalStateException("Email already taken!");
        }
        String encodedPass = bCryptPasswordEncoder.encode(appUser.getPassword());
        appUser.setPasswd(encodedPass);

        appUserRepositiory.save(appUser);
        String token = UUID.randomUUID().toString();
        ConfirmationToken confirmationToken = new ConfirmationToken(token,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(15),
                appUser);
        confirmationTokenService.saveConfirmationToken(confirmationToken);
        return token;
    }
    public int enableAppUser(String email){
        return appUserRepositiory.enableAppUser(email);
    }
}