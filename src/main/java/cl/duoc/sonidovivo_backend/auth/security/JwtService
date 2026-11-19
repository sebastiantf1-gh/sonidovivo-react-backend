package cl.duoc.sonidovivo_backend.auth.security;

import cl.duoc.sonidovivo_backend.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.issuer}")
    private String jwtIssuer;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    // El token lleva: sub (id), rol, iss, iat y exp. Nada sensible.
    public String generateToken(Usuario usuario, String nombreRol) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(usuario.getIdUsuario().toString())
                .claim("rol", nombreRol)
                .issuer(jwtIssuer)
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    // Lanza JwtException si la firma, el emisor o la expiración no son válidos.
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .requireIssuer(jwtIssuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getExpirationSeconds() {
        return jwtExpiration / 1000;
    }
}
