package bg.tusofia.taxiapp.exception;

public record ApiError(int status, String error, String message) {}
