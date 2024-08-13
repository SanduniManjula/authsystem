package authsystem.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpirationInMs;

    public String getUsernameFromJWT(String token) {
        return Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public Collection<? extends GrantedAuthority> getAuthorities(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();


        Integer roleId = claims.get("role_id", Integer.class);

        return roleId != null ?
                List.of(new SimpleGrantedAuthority("ROLE_" + roleId)) :
                List.of();
    }
/*
    public String generateToken(Authentication authentication) {
        String username = authentication.getName();


        Integer roleId = authentication.getAuthorities().stream()
                .findFirst()
                .map(auth -> {
                    try {
                        return Integer.parseInt(auth.getAuthority().replace("ROLE_", ""));
                    } catch (NumberFormatException e) {
                        throw new RuntimeException("Invalid role format in authority: " + auth.getAuthority());
                    }
                })
                .orElseThrow(() -> new RuntimeException("User has no roles assigned"));

        return Jwts.builder()
                .setSubject(username)
                .claim("role_id", roleId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + jwtExpirationInMs))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

 */
    public String generateToken(String username, long roleId) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", roleId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + jwtExpirationInMs))
                .signWith(SignatureAlgorithm.HS384, jwtSecret)
                .compact();
    }

    public String generateToken(Authentication authentication) {
        String username = authentication.getName();


        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + jwtExpirationInMs))
                .signWith(SignatureAlgorithm.HS384, jwtSecret)
                .compact();
    }

}
