package com.poultryfarm.business.mapper;

import com.poultryfarm.application.model.CreateDtoClient;
import com.poultryfarm.application.model.SearchClientsDto;
import com.poultryfarm.application.model.UpdateDtoClient;
import com.poultryfarm.persistence.entity.Cliente;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

/**
 * Mapper interface for converting between various client-related DTOs and the {@code Cliente} entity.
 * Uses MapStruct with Spring constructor injection.
 */
@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface ClienteMapper {

    Cliente clientDtoToClientEntity(CreateDtoClient createClientDto);

    Cliente updateClient(@MappingTarget Cliente clienteToBeUpdated, UpdateDtoClient createClientDto);

    SearchClientsDto clientToSearchClientsDto(Cliente cliente);
}
