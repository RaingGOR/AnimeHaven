package raingor.ru.userservice.exceptions;

import org.springframework.http.HttpStatus;
import raingor.ru.userservice.exceptions.core.ApiException;

public class UserNotFoundException extends ApiException {
    public UserNotFoundException() {
        super("User not found!", HttpStatus.NOT_FOUND);
    }
}

