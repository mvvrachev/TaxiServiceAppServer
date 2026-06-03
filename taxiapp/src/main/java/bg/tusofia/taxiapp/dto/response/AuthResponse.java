package bg.tusofia.taxiapp.dto.response;

import bg.tusofia.taxiapp.enums.Role;

public record AuthResponse(
        String token,
        Role role,
        Long userId
) {}
