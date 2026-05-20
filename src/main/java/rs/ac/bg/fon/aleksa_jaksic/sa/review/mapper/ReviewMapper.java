package rs.ac.bg.fon.aleksa_jaksic.sa.review.mapper;

import rs.ac.bg.fon.aleksa_jaksic.sa.review.domain.Review;
import rs.ac.bg.fon.aleksa_jaksic.sa.review.dtos.ReviewCreateUpdateDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.review.dtos.ReviewDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.review.dtos.ReviewDetailsDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ReviewMapper {

    Review toEntity(ReviewDTO reviewDTO);

    Review toEntity(ReviewCreateUpdateDTO reviewCreateUpdateDTO);

    @Mapping(source = "user.username", target = "username")
    ReviewDTO toDTO(Review review);

    @Mapping(source = "user.username", target = "username")
    ReviewDetailsDTO toDetailsDTO(Review review);

    void updateEntityFromUpdateDto(ReviewCreateUpdateDTO reviewCreateUpdateDTO, @MappingTarget Review review);
}
