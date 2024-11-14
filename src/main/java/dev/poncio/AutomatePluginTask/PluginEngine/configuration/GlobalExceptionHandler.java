package dev.poncio.AutomatePluginTask.PluginEngine.configuration;

import dev.poncio.AutomatePluginTask.PluginEngine.dto.ExceptionDTO;
import dev.poncio.AutomatePluginTask.PluginEngine.exceptions.BusinessException;
import dev.poncio.AutomatePluginTask.PluginEngine.exceptions.NewPluginRecordException;
import dev.poncio.AutomatePluginTask.PluginEngine.exceptions.PluginExecutionException;
import dev.poncio.AutomatePluginTask.PluginEngine.exceptions.PluginJarLoadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Object> businessExceptionHandler(BusinessException exception, WebRequest request) {
        return handleExceptionInternal(exception, ExceptionDTO.builder().message(exception.getMessage()).build(), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(NewPluginRecordException.class)
    public ResponseEntity<Object> newPluginRecordExceptionHandler(NewPluginRecordException exception, WebRequest request) {
        return handleExceptionInternal(exception, ExceptionDTO.builder().message(exception.getMessage()).build(), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(PluginJarLoadException.class)
    public ResponseEntity<Object> pluginJarLoadExceptionHandler(PluginJarLoadException exception, WebRequest request) {
        return handleExceptionInternal(exception, ExceptionDTO.builder().message(exception.getMessage()).build(), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(PluginExecutionException.class)
    public ResponseEntity<Object> pluginExecutionExceptionHandler(PluginExecutionException exception, WebRequest request) {
        return handleExceptionInternal(exception, ExceptionDTO.builder().message(exception.getMessage()).build(), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}