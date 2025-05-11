/**
 * Represents an error response from an HTTP request, encapsulating the status code and response body.
 * This exception is thrown when receiving non-success HTTP status codes (typically 4xx or 5xx responses).
 */
package org.volunteer.client.exception;

/**
 * Unchecked exception indicating an unsuccessful HTTP response.
 * Provides programmatic access to the HTTP status code and raw response body.
 *
 * <p>The exception message combines both status code and response body for readability,
 * while maintaining structured access to individual components through getter methods.
 *
 * @see RuntimeException
 */
public class HttpException extends RuntimeException {
    private final int statusCode;
    private final String responseBody;

    /**
     * Constructs a new HTTP exception with the specified status code and response body.
     *
     * @param statusCode The HTTP status code (must be ³ 400)
     * @param responseBody The raw response body content (may be empty or {@code null})
     */
    public HttpException(int statusCode, String responseBody) {
        super("HTTP error " + statusCode + ": " + responseBody);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    /**
     * Returns the HTTP status code that triggered this exception.
     *
     * @return HTTP status code (typically 4xx client error or 5xx server error)
     */
    public int getStatusCode() { return statusCode; }

    /**
     * Returns the raw response body received with the error response.
     *
     * @return Response content as string (may be empty or {@code null} if no body)
     */
    public String getResponseBody() { return responseBody; }
}