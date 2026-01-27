package com.runners.app.global.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;

public final class ApiErrorWriter {

    private ApiErrorWriter() {
    }

    public static void write(HttpServletResponse response, ObjectMapper objectMapper, int status, ErrorCode errorCode, String message)
            throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), new ApiErrorResponse(status, errorCode.name(), message));
    }
}

