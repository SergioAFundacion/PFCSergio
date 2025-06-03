package com.pfcsergio.backendspringboot.FiltrosProyecto;

import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface FiltrosProyectoMapper {

    List<FiltrosProyectoDTO> entitysToDTO(List<FiltrosProyecto> entity);

    FiltrosProyectoDTO entityToDTO(FiltrosProyecto entity);

    FiltrosProyecto DTOToEntity(FiltrosProyectoDTO dto);

    List<FiltrosProyecto> DTOsToEntitys(List<FiltrosProyectoDTO> dtos);

}