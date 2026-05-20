package rs.ac.bg.fon.aleksa_jaksic.sa.workday.mapper;

import rs.ac.bg.fon.aleksa_jaksic.sa.workday.domain.WorkDay;
import rs.ac.bg.fon.aleksa_jaksic.sa.workday.dtos.WorkDayCreateUpdateDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.workday.dtos.WorkDayDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface WorkDayMapper {

    WorkDay toEntity(WorkDayDTO workDayDTO);

    WorkDay toEntity(WorkDayCreateUpdateDTO workDayCreateUpdateDTO);

    WorkDayDTO toDTO(WorkDay workDay);

    void updateEntityFromUpdateDto(WorkDayCreateUpdateDTO workDayCreateUpdateDTO, @MappingTarget WorkDay workDay);
}
