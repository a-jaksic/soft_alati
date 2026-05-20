package rs.ac.bg.fon.aleksa_jaksic.sa.user.mapper;

import rs.ac.bg.fon.aleksa_jaksic.sa.user.domain.User;
import rs.ac.bg.fon.aleksa_jaksic.sa.security.dtos.UserLoginDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.user.dtos.UserRegisterDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.user.dtos.UserResponseDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.user.dtos.UserUpdateDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    User toEntity(UserRegisterDTO userRegisterDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "role", ignore = true)
    User toEntity(UserLoginDTO userLoginDTO);


    UserResponseDTO toResponseDTO(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    void updateEntityFromUpdateDto(UserUpdateDTO userUpdateDTO, @MappingTarget User user);

}
