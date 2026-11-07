package team.projectpulse.security;

import team.projectpulse.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;

/**
 * FR-SEC-active-account at the authentication path: the converter that stands between a decoded {@link Jwt}
 * and the {@link org.springframework.security.core.Authentication} the rest of the filter chain uses.
 */
@ExtendWith(MockitoExtension.class)
class ActiveAccountJwtAuthenticationConverterTest {

    @Mock
    UserService userService;

    private final JwtAuthenticationConverter delegate = new JwtAuthenticationConverter();

    private ActiveAccountJwtAuthenticationConverter converter;


    private Jwt jwtWithClaim(Object userIdClaim) {
        Jwt.Builder builder = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .subject("j.smith@abc.edu")
                .claim("authorities", "student");
        if (userIdClaim != null) {
            builder.claim("userId", userIdClaim);
        }
        return builder.build();
    }

    @Test
    @DisplayName("An active account converts to the delegate's own token, unaltered")
    void convertsActiveAccount() {
        this.converter = new ActiveAccountJwtAuthenticationConverter(this.delegate, this.userService);
        given(this.userService.isActiveAccount(4)).willReturn(true);

        Jwt jwt = jwtWithClaim(4);
        AbstractAuthenticationToken token = this.converter.convert(jwt);

        assertThat(token).isNotNull();
        assertThat(token.getPrincipal()).isEqualTo(jwt);
        assertThat(token.getName()).isEqualTo("j.smith@abc.edu");
    }

    @Test
    @DisplayName("A disabled account is refused even though the token itself is otherwise valid")
    void refusesDisabledAccount() {
        this.converter = new ActiveAccountJwtAuthenticationConverter(this.delegate, this.userService);
        given(this.userService.isActiveAccount(5)).willReturn(false);

        Throwable throwable = catchThrowable(() -> this.converter.convert(jwtWithClaim(5)));

        assertThat(throwable).isInstanceOf(DisabledException.class);
    }

    @Test
    @DisplayName("A userId naming no row at all is refused, not merely one that resolves to disabled")
    void refusesUnknownAccountId() {
        this.converter = new ActiveAccountJwtAuthenticationConverter(this.delegate, this.userService);
        given(this.userService.isActiveAccount(999999)).willReturn(false);

        Throwable throwable = catchThrowable(() -> this.converter.convert(jwtWithClaim(999999)));

        assertThat(throwable).isInstanceOf(DisabledException.class);
    }

    @Test
    @DisplayName("A missing userId claim is refused as an invalid token, before the account is ever looked up")
    void refusesMissingClaim() {
        this.converter = new ActiveAccountJwtAuthenticationConverter(this.delegate, this.userService);

        Throwable throwable = catchThrowable(() -> this.converter.convert(jwtWithClaim(null)));

        assertThat(throwable).isInstanceOf(InvalidBearerTokenException.class);
    }

    @Test
    @DisplayName("A non-numeric userId claim is refused as an invalid token")
    void refusesNonNumericClaim() {
        this.converter = new ActiveAccountJwtAuthenticationConverter(this.delegate, this.userService);

        Throwable throwable = catchThrowable(() -> this.converter.convert(jwtWithClaim("not-a-number")));

        assertThat(throwable).isInstanceOf(InvalidBearerTokenException.class);
    }

}
