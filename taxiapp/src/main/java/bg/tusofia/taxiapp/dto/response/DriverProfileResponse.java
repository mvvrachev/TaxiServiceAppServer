package bg.tusofia.taxiapp.dto.response;

public record DriverProfileResponse(
        String username,
        String firstName,
        String lastName,
        String phoneNumber,
        double averageRating,
        int ratingsCount,
        CarResponse car
) {}
