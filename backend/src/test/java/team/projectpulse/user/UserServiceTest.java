package team.projectpulse.user;

import team.projectpulse.user.resetpassword.ResetPasswordService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    ResetPasswordService resetPasswordService;
    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UserService userService;


    @Test
    void testIsActiveAccountForEnabledAccount() {
        given(this.userRepository.existsByIdAndEnabledTrue(4)).willReturn(true);

        assertThat(this.userService.isActiveAccount(4)).isTrue();
    }

    @Test
    void testIsActiveAccountForDisabledAccount() {
        given(this.userRepository.existsByIdAndEnabledTrue(5)).willReturn(false);

        assertThat(this.userService.isActiveAccount(5)).isFalse();
    }

    @Test
    void testIsActiveAccountForIdNamingNoRow() {
        given(this.userRepository.existsByIdAndEnabledTrue(999999)).willReturn(false);

        assertThat(this.userService.isActiveAccount(999999)).isFalse();
    }

    @Test
    void testIsActiveAccountForNullUserId() {
        assertThat(this.userService.isActiveAccount(null)).isFalse();
    }

}
