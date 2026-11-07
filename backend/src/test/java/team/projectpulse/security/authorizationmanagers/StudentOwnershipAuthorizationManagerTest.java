package team.projectpulse.security.authorizationmanagers;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import team.projectpulse.student.StudentSecurityService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * UC-STU-deactivate-student's ownership half of {@code anyOf(studentInstructor, studentOwnership)}. The
 * path-variable contract every {@code AuthorizationManager} in this package shares: the id comes from the
 * variables the matched route rule bound, and a variable the manager cannot resolve is a denial rather than
 * an exception.
 */
@ExtendWith(MockitoExtension.class)
class StudentOwnershipAuthorizationManagerTest {

    @Mock
    StudentSecurityService studentSecurityService;

    @InjectMocks
    StudentOwnershipAuthorizationManager studentOwnershipAuthorizationManager;


    private RequestAuthorizationContext contextWith(Map<String, String> variables) {
        return new RequestAuthorizationContext(mock(HttpServletRequest.class), variables);
    }

    @Test
    @DisplayName("Asks about the id the matched route bound, not one parsed out of the request URI")
    void delegatesTheBoundStudentId() {
        given(this.studentSecurityService.isCurrentUserAdminOfStudentCourse(4)).willReturn(true);

        AuthorizationResult result = this.studentOwnershipAuthorizationManager.authorize(() -> null, contextWith(Map.of("studentId", "4")));

        assertThat(result).isNotNull();
        assertThat(result.isGranted()).isTrue();
    }

    @Test
    @DisplayName("Denies when the matched route bound no studentId at all")
    void deniesWhenTheVariableIsAbsent() {
        AuthorizationResult result = this.studentOwnershipAuthorizationManager.authorize(() -> null, contextWith(Map.of()));

        assertThat(result).isNotNull();
        assertThat(result.isGranted()).isFalse();
        verifyNoInteractions(this.studentSecurityService);
    }

    @Test
    @DisplayName("Denies when the bound studentId is not a number")
    void deniesWhenTheVariableIsNotANumber() {
        AuthorizationResult result = this.studentOwnershipAuthorizationManager.authorize(() -> null, contextWith(Map.of("studentId", "not-a-number")));

        assertThat(result).isNotNull();
        assertThat(result.isGranted()).isFalse();
        verifyNoInteractions(this.studentSecurityService);
    }

    @Test
    @DisplayName("Denies when the named student does not exist, rather than throwing")
    void deniesWhenTheStudentDoesNotExist() {
        given(this.studentSecurityService.isCurrentUserAdminOfStudentCourse(999999)).willReturn(false);

        AuthorizationResult result = this.studentOwnershipAuthorizationManager.authorize(() -> null, contextWith(Map.of("studentId", "999999")));

        assertThat(result).isNotNull();
        assertThat(result.isGranted()).isFalse();
    }

}
