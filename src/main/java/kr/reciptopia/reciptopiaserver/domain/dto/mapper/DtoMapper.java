package kr.reciptopia.reciptopiaserver.domain.dto.mapper;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.util.Streamable;

public interface DtoMapper<ENTITY, DTO_CREATE, DTO_RESULT> {

    ENTITY asEntity(DTO_CREATE dto);

    DTO_RESULT asResultDto(ENTITY entity);

    default List<DTO_RESULT> asResultDto(Streamable<ENTITY> entities) {
        return entities.stream()
            .map(this::asResultDto)
            .collect(Collectors.toList());
    }
}
