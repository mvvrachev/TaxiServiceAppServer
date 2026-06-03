package bg.tusofia.taxiapp.dto.response;

import bg.tusofia.taxiapp.enums.Role;

public record UserResponse(
        Long id,
        String username,
        String firstName,
        String lastName,
        String phoneNumber,
        Role role
) {}
