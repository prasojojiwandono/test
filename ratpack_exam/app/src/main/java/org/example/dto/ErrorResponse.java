
package org.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorResponse {
    private String message;
    private int status;

    public ErrorResponse(String message, int status) {
        this.message = message;
        this.status = status;
    }

    @JsonProperty
    public String getMessage() {
        return message;
    }

    @JsonProperty
    public int getStatus() {
        return status;
    }
}
