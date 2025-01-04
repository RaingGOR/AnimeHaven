package raingor.ru.userservice.exceptions.core;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private String message;
    private String  timestamp;
    private int status;
    private String path;
}