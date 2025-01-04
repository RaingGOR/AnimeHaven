package raingor.ru.userservice.exceptions;

import org.springframework.http.HttpStatus;
import raingor.ru.userservice.exceptions.core.ApiException;

public class UserExistsWithThisEmailException extends ApiException {
    public UserExistsWithThisEmailException() {
        super("Email already in use", HttpStatus.CONFLICT);
    }
}
