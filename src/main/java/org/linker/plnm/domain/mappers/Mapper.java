package org.linker.plnm.domain.mappers;

import java.util.List;

public interface Mapper<Entity, Dto> {

    Entity toEntity(Dto dto);

    Dto toDto(Entity entity);

    List<Entity> toEntityList(List<Dto> dtoList);

    List<Dto> toDtoList(List<Entity> entityList);
}
