package team.projectpulse.student;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.projectpulse.course.Course;
import team.projectpulse.instructor.Instructor;
import team.projectpulse.section.Section;
import team.projectpulse.system.UserUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * UC-STU-deactivate-student: {@code isCurrentUserAdminOfStudentCourse} is one half of the route rule's
 * {@code anyOf(studentInstructor, studentOwnership)}. Every check fails closed: these methods run inside an
 * {@code AuthorizationManager}, ahead of the {@code DispatcherServlet}, so anything they throw escapes the
 * filter chain as a 500 instead of reaching {@code ExceptionHandlerAdvice}.
 */
@ExtendWith(MockitoExtension.class)
class StudentSecurityServiceTest {

    @Mock
    StudentRepository studentRepository;
    @Mock
    UserUtils userUtils;

    @InjectMocks
    StudentSecurityService studentSecurityService;

    Student student;
    Instructor courseAdmin;


    @BeforeEach
    void setUp() {
        this.courseAdmin = new Instructor("b.wei@abc.edu", "Bingyang", "Wei", "b.wei@abc.edu", "123456", true, "admin instructor");
        this.courseAdmin.setId(1);

        Course course = new Course("COSC 40993 Senior Design", "Senior design project course");
        course.setCourseAdmin(this.courseAdmin);

        Section section = new Section();
        section.setSectionId(2);
        section.setCourse(course);

        this.student = new Student("j.smith@abc.edu", "John", "Smith", "j.smith@abc.edu", "123456", true, "student");
        this.student.setId(4);
        this.student.setSection(section);
    }

    @Test
    @DisplayName("The admin of the course that owns the student's section is granted")
    void isCurrentUserAdminOfStudentCourseGrantsTheOwningCourseAdmin() {
        given(this.studentRepository.findById(4)).willReturn(Optional.of(this.student));
        given(this.userUtils.getUserId()).willReturn(1);

        assertThat(this.studentSecurityService.isCurrentUserAdminOfStudentCourse(4)).isTrue();
    }

    @Test
    @DisplayName("An admin of a different course is denied")
    void isCurrentUserAdminOfStudentCourseDeniesAnotherCoursesAdmin() {
        given(this.studentRepository.findById(4)).willReturn(Optional.of(this.student));
        given(this.userUtils.getUserId()).willReturn(999);

        assertThat(this.studentSecurityService.isCurrentUserAdminOfStudentCourse(4)).isFalse();
    }

    @Test
    @DisplayName("A student id that names no row is a denial, not an exception")
    void isCurrentUserAdminOfStudentCourseDeniesWhenTheStudentDoesNotExist() {
        given(this.studentRepository.findById(999999)).willReturn(Optional.empty());

        assertThat(this.studentSecurityService.isCurrentUserAdminOfStudentCourse(999999)).isFalse();
    }

    @Test
    @DisplayName("A deactivated student is still resolved: this check must not filter on enabled")
    void isCurrentUserAdminOfStudentCourseDoesNotFilterOnEnabled() {
        this.student.setEnabled(false);
        given(this.studentRepository.findById(4)).willReturn(Optional.of(this.student));
        given(this.userUtils.getUserId()).willReturn(1);

        assertThat(this.studentSecurityService.isCurrentUserAdminOfStudentCourse(4)).isTrue();
    }

}
