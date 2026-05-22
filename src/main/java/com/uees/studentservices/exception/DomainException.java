package com.uees.studentservices.exception;

import org.springframework.http.HttpStatus;

public class DomainException extends RuntimeException {

    private final HttpStatus status;

    public DomainException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() { return status; }

    public static DomainException notFound(String msg) {
        return new DomainException(HttpStatus.NOT_FOUND, msg);
    }

    public static DomainException badRequest(String msg) {
        return new DomainException(HttpStatus.BAD_REQUEST, msg);
    }

    public static DomainException conflict(String msg) {
        return new DomainException(HttpStatus.CONFLICT, msg);
    }

    public static DomainException forbidden(String msg) {
        return new DomainException(HttpStatus.FORBIDDEN, msg);
    }
}
