package dev.hazoe.springsecuritydemo.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {
    //The same at all times
    private final Key key;

    public JwtService() {
        byte[] keyBytes = Decoders.BASE64.decode(
                "TmV3U2VjcmV0S2V5Rm9ySldUU29uZ2luZ21hc2sgZmF2b3I="
        );
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String getAccessToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 3))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

}
