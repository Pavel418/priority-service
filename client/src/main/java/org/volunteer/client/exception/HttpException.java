// HttpException.java
package org.volunteer.client.exception;

public class HttpException extends RuntimeException {
    private final int statusCode;
    private final String responseBody;

    public HttpException(int statusCode, String responseBody) {
        super("HTTP error " + statusCode + ": " + responseBody);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int getStatusCode() { return statusCode; }
    public String getResponseBody() { return responseBody; }
}