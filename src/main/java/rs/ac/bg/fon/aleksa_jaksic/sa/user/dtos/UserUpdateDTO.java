package rs.ac.bg.fon.aleksa_jaksic.sa.user.dtos;

public record UserUpdateDTO(

        String username,

        String email,

        String currentPassword,

        String newPassword
)
{}
