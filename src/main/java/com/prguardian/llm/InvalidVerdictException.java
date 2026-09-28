package com.prguardian.llm;

/**
 * Thrown when the LLM's response can't be parsed into a valid ReviewVerdict,
 * or parses but violates our own constraints (e.g. riskScore out of range).
 * Caught by ReviewService and turned into a FAILED PR status rather than
 * silently storing garbage or crashing the request.
 */
public class InvalidVerdictException extends RuntimeException {

    private final String rawResponse;

    public InvalidVerdictException(String message, String rawResponse, Throwable cause) {
        super(message, cause);
        this.rawResponse = rawResponse;
    }

    public InvalidVerdictException(String message, String rawResponse) {
        super(message);
        this.rawResponse = rawResponse;
    }

    public String getRawResponse() {
        return rawResponse;
    }
}