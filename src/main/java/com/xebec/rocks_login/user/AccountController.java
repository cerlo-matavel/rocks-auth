package com.xebec.rocks_login.user;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Optional;

@RestController
@RequestMapping("/account")
public class AccountController {

    @Value("${security.jwt.secretkey}")
    private String jwtSecretKey;

    @Value("${security.jwt.issuer}")
    private String jwtIssuer;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private AppUserService appUserService;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    public AccountController(AppUserService appUserService,
                             AppUserRepository appUserRepository,
                             AuthenticationManager authenticationManager) {
        this.appUserService = appUserService;
        this.appUserRepository = appUserRepository;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public ResponseEntity<Object> register(
            @Valid @RequestBody RegisterDTO registerDTO , BindingResult result
    ){
        if (result.hasErrors()) {

            var errorList = result.getAllErrors();

            var errorMap = new HashMap<String, String>();

            for (var error : errorList) {
                errorMap.put(error.getDefaultMessage(), error.getDefaultMessage());
            }

            return ResponseEntity.badRequest().body(errorMap);
        }

        var bCryptEncoder = new BCryptPasswordEncoder();

        AppUser appUser = new AppUser(
                registerDTO.username(),
                registerDTO.email(),
                bCryptEncoder.encode(registerDTO.password()),
                "client"
        );

        try{
            var otherUser = appUserRepository.findByUsername(registerDTO.username());
            if (otherUser.isPresent()) {
                return ResponseEntity.badRequest().body("Username already exists");
            }

            appUserRepository.save(appUser);

            String jwtToken = createJwtToken(appUser);

            var response = new HashMap<String, Object>();
            response.put("token", jwtToken);
            response.put("username", appUser);
            return ResponseEntity.ok(response);
        }catch (Exception e){
            System.out.println("An error has occured:");
            e.printStackTrace();
        }

        return ResponseEntity.badRequest().body("Bad Request");

    }

    @GetMapping("/profile")
    public ResponseEntity<Object> profile(Authentication authentication){
        var response = new HashMap<String, Object>();
        response.put("username", authentication.getName());
        response.put("Authorities", authentication.getAuthorities());

        Optional<AppUser> appUser = appUserRepository.findByUsername(authentication.getName());
        appUser.ifPresent(user -> response.put("user", user));
        return ResponseEntity.ok(response);

    }
    @PostMapping("/login")
    public ResponseEntity<Object> login(@Valid @RequestBody LoginDTO loginDTO , BindingResult result){
        if (result.hasErrors()) {
            var errorList = result.getAllErrors();
            var errorMap = new HashMap<String, String>();
            for (var error : errorList) {
                errorMap.put(error.getDefaultMessage(), error.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(errorMap);
        }

        try{
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginDTO.username(),
                    loginDTO.password()
            ));

            AppUser appUser = appUserRepository.findByUsername(loginDTO.username()).get();

            String jwtToken = createJwtToken(appUser);

            var response = new HashMap<String, Object>();
            response.put("token", jwtToken);
            response.put("username", appUser);

            return ResponseEntity.ok(response);
        }catch (Exception e){
            System.out.println("An error has occured:");
            e.printStackTrace();
        }

        return ResponseEntity.badRequest().body("Bad username or password");
    }

    private String createJwtToken(AppUser appUser) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(Instant.now())
                .issuer(jwtIssuer)
                .expiresAt(Instant.now().plusSeconds(24 * 3600))
                .subject(appUser.getUsername())
                .claim("role", appUser.getRole())
                .build();

        var encoder = new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey.getBytes()));
        var params = JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims);

        return encoder.encode(params).getTokenValue();
    }
}
