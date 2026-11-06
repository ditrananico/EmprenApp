package org.emprenApp.auth.infrastructure.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;


import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.entry;


class JwtAuthenticationEntryPointTest {

    private JwtAuthenticationEntryPoint entryPoint;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        entryPoint = new JwtAuthenticationEntryPoint(new ObjectMapper());
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    static Stream<AuthenticationException> authenticationExceptions() {
        return Stream.of(
                new BadCredentialsException("credenciales malas"),
                new InsufficientAuthenticationException("falta autenticacion"),
                new AccountExpiredException("cuenta vencida"));
    }

    // Estado HTTP │ commence responde 401 (response.getStatus() == 401). Es lo que ve el cliente cuando falta
    // el token, esta vencido o es invalido.
    @Test
    @DisplayName("ENTRYPOINT: responde 401")
    void respondsUnauthorized() throws Exception {
        entryPoint.commence(request, response, new BadCredentialsException("sin token"));

        assertThat(response.getStatus()).isEqualTo(401);
    }

    // Formato │ El Content-Type es JSON (application/json) y el encoding UTF-8. Cuidado: getContentType() puede incluir
    // ";charset=UTF-8"; usar startsWith("application/json") o isEqualTo con el valor exacto que veas al imprimirlo.
    @Test
    @DisplayName("ENTRYPOINT: responde JSON en UTF-8")
    void respondsJson() throws Exception {
        entryPoint.commence(request, response, new BadCredentialsException("sin token"));

        assertThat(response.getContentType()).startsWith("application/json");
    }

    // Body │ El body es exactamente {"code":"UNAUTHORIZED"}: leerlo con objectMapper.readTree(body) y comprobar
    // que tiene un unico campo "code" con valor "UNAUTHORIZED" (assertThat(json.get("code").asText()) y json.size() == 1,
    // para que no se filtre ningun otro dato, por ejemplo el mensaje de la excepcion).
    @Test
    @DisplayName("ENTRYPOINT: el body trae solo code UNAUTHORIZED")
    void bodyHasUnauthorizedCode() throws Exception {
        entryPoint.commence(request, response, new BadCredentialsException("sin token"));

        JsonNode json = new ObjectMapper().readTree(response.getContentAsString());

        assertThat(json.get("code").asText()).isEqualTo("UNAUTHORIZED");
        assertThat(json.size()).isEqualTo(1);    }

    // No filtra informacion │ El motivo real de la excepcion (p. ej. new BadCredentialsException("detalle interno secreto"))
    // NO aparece en el body: assertThat(body).doesNotContain("detalle interno secreto"). El cliente solo ve UNAUTHORIZED.
    @Test
    @DisplayName("ENTRYPOINT: no expone el mensaje de la excepcion")
    void doesNotLeakExceptionMessage() throws Exception {
        entryPoint.commence(request, response, new BadCredentialsException("detalle interno secreto"));

        String body = response.getContentAsString();
        assertThat(body).doesNotContain("detalle interno secreto");
    }

    // Cualquier excepcion │ Mismo resultado (401 y mismo body) sin importar la AuthenticationException recibida
    // (BadCredentialsException, InsufficientAuthenticationException, o incluso null: el codigo no la usa).
    // Se puede hacer como @ParameterizedTest + @MethodSource, igual que anyAuthenticationException en AuthServiceTest.

    @ParameterizedTest(name = "ENTRYPOINT: responde 401 con {0}")
    @NullSource
    @MethodSource("authenticationExceptions")
    void sameResponseForAnyException(AuthenticationException exception) throws Exception {
        entryPoint.commence(request, response, exception);

        assertThat(response.getStatus()).isEqualTo(401);

        Map<String, Object> body = new ObjectMapper()
                .readValue(response.getContentAsString(), new TypeReference<>() {});
        assertThat(body).containsOnly(entry("code", "UNAUTHORIZED"));
    }
}
