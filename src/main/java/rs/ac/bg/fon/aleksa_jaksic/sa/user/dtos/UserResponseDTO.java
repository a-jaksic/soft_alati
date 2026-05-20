package rs.ac.bg.fon.aleksa_jaksic.sa.user.dtos;

import rs.ac.bg.fon.aleksa_jaksic.sa.user.domain.Role;

public record UserResponseDTO(

        Long id,

        String username,

        String email,

        Role role
)
{}
