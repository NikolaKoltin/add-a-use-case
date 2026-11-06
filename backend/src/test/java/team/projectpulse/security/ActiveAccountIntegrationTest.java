package team.projectpulse.security;

import jakarta.persistence.EntityManager;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import team.projectpulse.AbstractIntegrationTest;
import team.projectpulse.student.Student;
import team.projectpulse.student.StudentRepository;
import team.projectpulse.system.StatusCode;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * FR-SEC-active-account: a deactivated or deleted account is denied authentication and access to protected
 * resources, regardless of an otherwise-valid credential or an otherwise-valid, unexpired JWT.
 *
 * <p>Login already enforced this (Spring Security's {@code DaoAuthenticationProvider} checks
 * {@code UserDetails.isEnabled()}); what {@code ActiveAccountJwtAuthenticationConverter} closes is the
 * remaining gap: a token minted before deactivation, still within its 2-hour lifetime, used after.
 */
@DisplayName("Integration tests for FR-SEC-active-account")
class ActiveAccountIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    EntityManager entityManager;

    @Value("${api.endpoint.base-url}")
    String baseUrl;


    private String login(String email, String password) throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic(email, password))).andReturn();
        JSONObject json = new JSONObject(mvcResult.getResponse().getContentAsString());
        return "Bearer " + json.getJSONObject("data").getString("token");
    }

    private void deactivate(Integer studentId) {
        Student student = this.studentRepository.findById(studentId).orElseThrow();
        student.setEnabled(false);
        this.studentRepository.save(student);
        this.entityManager.flush();
    }

    @Test
    @DisplayName("A token minted before deactivation is refused on the next API call")
    void tokenMintedBeforeDeactivationIsRefusedOnNextRequest() throws Exception {
        // Eric (id 5) logs in while still active, then is deactivated mid-session
        String ericToken = login("e.hudson@abc.edu", "123456");
        deactivate(5);

        this.mockMvc.perform(get(this.baseUrl + "/students/5").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, ericToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value("User account is abnormal."));
    }

    @Test
    @DisplayName("Login with a deactivated student's credentials is refused")
    void loginAfterDeactivationIsRefused() throws Exception {
        deactivate(5);

        this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic("e.hudson@abc.edu", "123456")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("An active student's token still reaches a route it reached before")
    void activeStudentTokenStillReachesHerOwnRoute() throws Exception {
        String johnToken = login("j.smith@abc.edu", "123456");

        this.mockMvc.perform(get(this.baseUrl + "/students/4").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, johnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS));
    }

}
