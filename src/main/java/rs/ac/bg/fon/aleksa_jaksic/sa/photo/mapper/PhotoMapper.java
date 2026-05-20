package rs.ac.bg.fon.aleksa_jaksic.sa.photo.mapper;

import rs.ac.bg.fon.aleksa_jaksic.sa.photo.domain.Photo;
import rs.ac.bg.fon.aleksa_jaksic.sa.photo.dtos.PhotoDTO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PhotoMapper {

    Photo toEntity(PhotoDTO photoDTO);

    PhotoDTO toDTO(Photo photo);
}
