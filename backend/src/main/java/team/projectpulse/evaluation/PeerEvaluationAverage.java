package team.projectpulse.evaluation;

import java.util.List;

public class PeerEvaluationAverage {

    private Integer studentId;
    private String week;
    private String firstName;
    private String lastName;
    private String email;
    private String teamName;
    private boolean enabled;
    private Double averageTotalScore;
    private List<String> publicComments;
    private List<String> privateComments;
    private List<RatingAverage> ratingAverages;


    public PeerEvaluationAverage() {
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public String getWeek() {
        return week;
    }

    public void setWeek(String week) {
        this.week = week;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    /**
     * Whether the student's account is currently active (BR-student-lifecycle), set from
     * {@code Student.isEnabled()} as of when this average was generated. A deactivated teammate still appears
     * in a week's report she was on the team for; this is what tells the UI to mark her inactive rather than
     * read her {@code averageTotalScore} as a 0.0.
     */
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Double getAverageTotalScore() {
        return averageTotalScore;
    }

    public void setAverageTotalScore(Double averageTotalScore) {
        this.averageTotalScore = averageTotalScore;
    }

    public List<String> getPublicComments() {
        return publicComments;
    }

    public void setPublicComments(List<String> publicComments) {
        this.publicComments = publicComments;
    }

    public List<String> getPrivateComments() {
        return privateComments;
    }

    public void setPrivateComments(List<String> privateComments) {
        this.privateComments = privateComments;
    }

    public List<RatingAverage> getRatingAverages() {
        return ratingAverages;
    }

    public void setRatingAverages(List<RatingAverage> ratingAverages) {
        this.ratingAverages = ratingAverages;
    }

}
