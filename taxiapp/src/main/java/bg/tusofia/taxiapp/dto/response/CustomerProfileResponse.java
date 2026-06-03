package bg.tusofia.taxiapp.dto.response;

public record CustomerProfileResponse(
        String username,
        String firstName,
        String lastName,
        String phoneNumber
) {}
