package bg.tusofia.taxiapp.dto.response;

public record RatingResponse(
        Long id,
        Long rideId,
        int score
) {}
