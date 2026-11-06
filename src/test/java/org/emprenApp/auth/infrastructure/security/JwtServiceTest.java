package org.emprenApp.auth.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.security.WeakKeyException;
import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

import static org.assertj.core.api.Assertions.*;


class JwtServiceTest {

    private static final String SECRET = encode("clave-de-test-solo-para-jwt-0123456789ab");


    private static final String OTHER_SECRET = encode("otra-clave-de-test-distinta-0123456789ab");

    private static final long EXPIRATION_MINUTES = 60;

    private static final String EMAIL = "a@b.com";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, EXPIRATION_MINUTES);
    }

    private static String encode(String rawKey) {
        return Base64.getEncoder().encodeToString(rawKey.getBytes(StandardCharsets.UTF_8));
    }

    // ---------------------------------------------------------------------------------------------
    // Generar y extraer │ generateToken("a@b.com") y luego extractUsername devuelve el mismo email.
    // ---------------------------------------------------------------------------------------------
    @Test
    @DisplayName("TOKEN: generar y extraer devuelve el mismo email")
    void generateAndExtract() {
        String jwt = jwtService.generateToken(EMAIL);
        String email = jwtService.extractUsername(jwt);

        assertThat(email).isEqualTo(EMAIL);

    }

    // Estructura │ El token tiene 3 partes separadas por puntos.
    @Test
    @DisplayName("TOKEN: tiene tres partes separadas por puntos")
    void tokenStructure() {
        String jwt = jwtService.generateToken(EMAIL);
        String[] parts = jwt.split("\\.");

        assertThat(parts).hasSize(3);
    }

    // Vencimiento │ exp - iat es igual a la duracion configurada, y getExpirationSeconds() devuelve 3600 con 60 minutos.
    @Test
    @DisplayName("TOKEN: el vencimiento es iat mas la duracion configurada")
    void expirationMatchesConfig() {
        String jwt = jwtService.generateToken(EMAIL);
        Claims claims = jwtService.extractClaims(jwt);
        long seconds = (claims.getExpiration().getTime() - claims.getIssuedAt().getTime()) / 1000;

        assertThat(seconds).isEqualTo(EXPIRATION_MINUTES * 60);
        assertThat(jwtService.getExpirationSeconds()).isEqualTo(EXPIRATION_MINUTES * 60);
    }

    // Token vencido │ Con duracion negativa, extractClaims lanza ExpiredJwtException.
    // (Usar -1 y no 0: con 0, exp == iat y el resultado depende del reloj, test inestable.)
    @Test
    @DisplayName("PARSE: token vencido lanza ExpiredJwtException")
    void expiredToken() {
        JwtService expiredJwtService = new JwtService(SECRET, -1);
        String jwt = expiredJwtService.generateToken(EMAIL);

        assertThatThrownBy(() -> expiredJwtService.extractClaims(jwt))
                .isInstanceOf(ExpiredJwtException.class);
    }

    // Firma alterada │ Cambiar un caracter del principio de la firma lanza SignatureException.
    // (No tocar el ultimo caracter: solo aporta 4 bits y puede no cambiar los bytes.)
    @Test
    @DisplayName("PARSE: firma alterada lanza SignatureException")
    void tamperedSignature() {
        String jwt = jwtService.generateToken(EMAIL);

        int lastDot = jwt.lastIndexOf('.');
        String headerAndPayload = jwt.substring(0, lastDot);
        String signature = jwt.substring(lastDot + 1);
        char first = signature.charAt(0);
        char changed = first == 'A' ? 'B' : 'A';
        String tamperedSignature = changed + signature.substring(1);
        String tamperedJwt = headerAndPayload + "." + tamperedSignature;

        assertThatThrownBy(() -> jwtService.extractClaims(tamperedJwt))
                .isInstanceOf(SignatureException.class);

    }

    // Otra clave │ Un token generado por otro JwtService con distinta clave (OTHER_SECRET) lanza SignatureException.
    @Test
    @DisplayName("PARSE: token firmado con otra clave lanza SignatureException")
    void tokenSignedWithOtherKey() {
        JwtService expiredJwtService = new JwtService(OTHER_SECRET, 60);
        String jwtOther = expiredJwtService.generateToken(EMAIL);

        assertThatThrownBy(() -> jwtService.extractClaims(jwtOther))
                .isInstanceOf(SignatureException.class);
    }

    // Mal formado │ "abc.def.ghi" lanza MalformedJwtException, y un token vacio lanza IllegalArgumentException.
    @Test
    @DisplayName("PARSE: token mal formado o vacio lanza excepcion")
    void malformedOrEmptyToken() {
        assertThatThrownBy(() -> jwtService.extractClaims("abc.def.ghi"))
                .isInstanceOf(MalformedJwtException.class);
        assertThatThrownBy(() -> jwtService.extractClaims(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // Clave debil │ Una clave de menos de 32 bytes lanza WeakKeyException en el constructor.
    @Test
    @DisplayName("CONSTRUCTOR: clave de menos de 32 bytes lanza WeakKeyException")
    void weakKey() {
        String secredt = encode("corta");
        assertThatThrownBy(() -> new JwtService(secredt, EXPIRATION_MINUTES))
                .isInstanceOf(WeakKeyException.class);

    }

    // Payload alterado │ Cambiar el sub (payload) conservando la firma original lanza SignatureException.
    // Es el ataque real: "quiero ser otro usuario". Se arma cambiando la 2da parte del token.
    @Test
    @DisplayName("PARSE: payload alterado con la firma original lanza SignatureException")
    void tamperedPayload() {

        String jwt = jwtService.generateToken(EMAIL);
        String[] parts = jwt.split("\\.");

        String json = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        String forgedJson = json.replace(EMAIL, "admin@b.com");
        String forgedPayload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(forgedJson.getBytes(StandardCharsets.UTF_8));

        String tamperedJwt = parts[0] + "." + forgedPayload + "." + parts[2];

        assertThatThrownBy(() -> jwtService.extractClaims(tamperedJwt))
                .isInstanceOf(SignatureException.class);
    }

    // Sin firma (alg none) │ Un token con header {"alg":"none"} y sin firma lanza UnsupportedJwtException.
    // Prueba que no se puede saltar la verificacion declarando que no hay algoritmo.
    @Test
    @DisplayName("PARSE: token sin firma (alg none) es rechazado")
    void unsignedToken() {
        String jwt = jwtService.generateToken(EMAIL);
        String[] parts = jwt.split("\\.");
        String noneHeader = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"none\"}".getBytes(StandardCharsets.UTF_8));

        String tamperedJwt = noneHeader + "." + parts[1] + ".";

        assertThatThrownBy(() -> jwtService.extractClaims(tamperedJwt))
                .isInstanceOf(UnsupportedJwtException.class);

    }

    // Claims │ extractClaims devuelve subject, issuedAt y expiration no nulos, y issuedAt cercano a "ahora".
    @Test
    @DisplayName("TOKEN: extractClaims trae subject, iat y exp")
    void claimsContent() {
        String jwt = jwtService.generateToken(EMAIL);
        Claims claims = jwtService.extractClaims(jwt);

        assertThat(claims.getSubject()).isEqualTo(EMAIL);
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isNotNull();
        assertThat(claims.getIssuedAt()).isCloseTo(new Date(), 5000);
    }

    // Clave en el limite │ Una clave de exactamente 32 bytes es aceptada (no lanza excepcion).
    @Test
    @DisplayName("CONSTRUCTOR: clave de exactamente 32 bytes es valida")
    void minimumKeyLength() {
        String key32 = encode("12345678901234567890123456789012");   // 32 caracteres ASCII = 32 bytes
        assertThatCode(() -> new JwtService(key32, EXPIRATION_MINUTES))
                .doesNotThrowAnyException();
    }
}
