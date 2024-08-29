package senior.copycoders.project.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@Controller
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ErrorDto handleException(Exception ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR; // По умолчанию
        String errorDescription = ex.getMessage();

        // Проверяем, есть ли у исключения аннотация @ResponseStatus
        ResponseStatus responseStatus = ex.getClass().getAnnotation(ResponseStatus.class);
        if (responseStatus != null) {
            status = responseStatus.value();
        }

        return new ErrorDto(String.valueOf(status.value()), errorDescription);

    }
}
