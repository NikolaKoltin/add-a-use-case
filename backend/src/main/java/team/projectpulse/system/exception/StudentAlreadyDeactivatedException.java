package team.projectpulse.system.exception;

public class StudentAlreadyDeactivatedException extends RuntimeException {

    public StudentAlreadyDeactivatedException(String message) {
        super(message);
    }

}
