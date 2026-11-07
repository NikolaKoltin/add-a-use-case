package team.projectpulse.system;

import team.projectpulse.instructor.Instructor;
import team.projectpulse.instructor.InstructorRepository;
import team.projectpulse.student.Student;
import team.projectpulse.student.StudentRepository;
import team.projectpulse.system.exception.ObjectNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class UserUtils {

    private final InstructorRepository instructorRepository;
    private final StudentRepository studentRepository;


    public UserUtils(InstructorRepository instructorRepository, StudentRepository studentRepository) {
        this.instructorRepository = instructorRepository;
        this.studentRepository = studentRepository;
    }

    public Integer getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;

        Object principal = auth.getPrincipal();
        if (!(principal instanceof Jwt jwt)) return null;

        return parseUserIdClaim(jwt.getClaim("userId"));
    }

    /**
     * Reads the {@code userId} claim {@link team.projectpulse.security.JwtProvider} signs (an {@code Integer}
     * in practice), tolerating the {@code Long}/numeric-{@code String} shapes a JWT library can hand back.
     * Shared with {@link team.projectpulse.security.ActiveAccountJwtAuthenticationConverter}, which reads this
     * same claim directly off the {@code Jwt} before an {@code Authentication} exists for {@link #getUserId()}
     * to read it from.
     *
     * @return the claim as an id, or {@code null} if it is missing or not a recognizable number
     */
    public static Integer parseUserIdClaim(Object claim) {
        if (claim instanceof Integer i) return i;
        if (claim instanceof Long l) return l.intValue();
        if (claim instanceof String s) {
            try {
                return Integer.valueOf(s);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        return null;
    }

    public Integer getUserCourseId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean hasInstructorRole = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_instructor"));
        // If the user is an instructor, return the default course id of the instructor
        // If the user is a student, return the course id of the student
        if (hasInstructorRole) {
            return getInstructorDefaultCourseId();
        } else {
            return getStudent().getSection().getCourse().getCourseId();
        }
    }

    public Integer getUserSectionId() {
        // If the user is an instructor, return the default section id of the instructor
        // If the user is a student, return the section id of the student
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean hasInstructorRole = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_instructor"));
        if (hasInstructorRole) {
            return getInstructorDefaultSectionId();
        } else {
            return getStudent().getSection().getSectionId();
        }
    }

    public Integer getInstructorDefaultCourseId() {
        Integer userId = getUserId();

        Instructor instructor = this.instructorRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("instructor", userId));

        if (instructor.getDefaultCourse() == null) {
            throw new IllegalArgumentException("Instructor does not have a default course");
        }

        return instructor.getDefaultCourse().getCourseId();
    }

    public Integer getInstructorDefaultSectionId() {
        Integer userId = getUserId();

        Instructor instructor = this.instructorRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("instructor", userId));

        if (instructor.getDefaultSection() == null) {
            throw new IllegalArgumentException("Instructor does not have a default section");
        }

        return instructor.getDefaultSection().getSectionId();
    }

    public Instructor getInstructor() {
        Integer userId = getUserId();
        return this.instructorRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("instructor", userId));
    }

    public Student getStudent() {
        Integer userId = getUserId();
        return this.studentRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("student", userId));
    }

    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(role));
    }
}
