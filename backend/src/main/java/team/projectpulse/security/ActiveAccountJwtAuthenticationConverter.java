package team.projectpulse.security;

import team.projectpulse.user.UserService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

/**
 * Closes FR-SEC-active-account at every request, not only at login: a JWT is good for up to 2 hours
 * ({@link team.projectpulse.security.JwtProvider}), and without this check a deactivated account's
 * already-issued token would keep working for however much of that window remained.
 *
 * <p>Delegates to the plain {@link JwtAuthenticationConverter} first — so authorities are unaffected — then
 * resolves the signed {@code userId} claim and asks {@link UserService#isActiveAccount} whether that account
 * may still act. A missing or non-numeric claim is refused the same way an otherwise-malformed token is
 * ({@link InvalidBearerTokenException}); a claim naming a deactivated or deleted account throws
 * {@link DisabledException}, which lands on the existing {@code AccountStatusException} handler in
 * {@code ExceptionHandlerAdvice} ({@code 401}, "User account is abnormal.").
 *
 * <p>This runs for every bearer-authenticated request, instructors included, so it also closes the same gap
 * for UC-INS-deactivate-instructor for free.
 */
public class ActiveAccountJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtAuthenticationConverter delegate;
    private final UserService userService;


    public ActiveAccountJwtAuthenticationConverter(JwtAuthenticationConverter delegate, UserService userService) {
        this.delegate = delegate;
        this.userService = userService;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        AbstractAuthenticationToken authentication = this.delegate.convert(jwt);

        Integer userId = readUserId(jwt.getClaim("userId"));
        if (userId == null) {
            throw new InvalidBearerTokenException("The access token does not name a valid account.");
        }

        if (!this.userService.isActiveAccount(userId)) {
            throw new DisabledException("User account is disabled.");
        }

        return authentication;
    }

    private static Integer readUserId(Object claim) {
        if (claim instanceof Integer i) {
            return i;
        }
        if (claim instanceof Long l) {
            return l.intValue();
        }
        if (claim instanceof String s) {
            try {
                return Integer.valueOf(s);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        return null;
    }

}
