package com.poultryfarm.business.service;

import com.poultryfarm.application.model.PageInfo;
import com.poultryfarm.application.model.CreateDtoClient;
import com.poultryfarm.application.model.SearchClientResponse;
import com.poultryfarm.application.model.SearchData;
import com.poultryfarm.application.model.UpdateDtoClient;
import com.poultryfarm.business.exception.ClientNotFoundException;
import com.poultryfarm.business.exception.InvalidFiscalCodeException;
import com.poultryfarm.business.mapper.ClienteMapper;
import com.poultryfarm.business.specification.ClientSpecification;
import com.poultryfarm.business.util.FiscalCodeValidator;
import com.poultryfarm.persistence.entity.Cliente;
import com.poultryfarm.persistence.repository.ClienteRepository;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;
    private final ClientSpecification clientSpecification;
    private static final String CLIENT_NOT_FOUND_MESSAGE = "client with id %d not found";

    @Transactional
    public void markAsDeleted(Long userId) throws ClientNotFoundException {
        clienteRepository.findByIdAndEliminatoFalse(userId)
                .map(client -> {
                    client.setEliminato(true);
                    return clienteRepository.save(client);
                })
                .orElseThrow(() -> new ClientNotFoundException(
                        String.format(CLIENT_NOT_FOUND_MESSAGE, userId)
                ));
    }

    @Transactional(readOnly = true)
    public Optional<Cliente> findById(Long id) {
        return this.clienteRepository.findByIdAndEliminatoFalse(id);
    }

    @Transactional(readOnly = true)
    public SearchClientResponse findAll(PageInfo pageInfo, SearchData searchData, boolean isPaginated) {

        if (isPaginated) {
            Page<Cliente> page = this.clienteRepository
                    .findAll(this.clientSpecification.getNomeCognomeIndirizzoLikeSpecs(searchData),
                            PageRequest.of(pageInfo.getPageNumber(), pageInfo.getPageSize()));
            PageInfo pageInfoResponse = PageInfo
                    .builder()
                    .totalPages(page.getTotalPages())
                    .totalElements(page.getTotalElements())
                    .pageNumber(page.getPageable().getPageNumber())
                    .pageSize(page.getPageable().getPageSize())
                    .build();
            return SearchClientResponse
                    .builder()
                    .searchClientsDtoList(page
                            .stream()
                            .map(this.clienteMapper::clientToSearchClientsDto)
                            .toList())
                    .pageInfo(pageInfoResponse).build();
        } else {
            return SearchClientResponse
                    .builder()
                    .searchClientsDtoList(this.clienteRepository
                            .findAll(this.clientSpecification.getNomeCognomeIndirizzoLikeSpecs(searchData))
                            .stream()
                            .map(this.clienteMapper::clientToSearchClientsDto)
                            .toList())
                    .build();
        }
    }

    @Transactional
    public Cliente save(CreateDtoClient clientDto) throws InvalidFiscalCodeException {
        validateInput(clientDto);
        validateFiscalCode(clientDto.getCodiceFiscale());

        Cliente cliente = clienteMapper.clientDtoToClientEntity(clientDto);
        return clienteRepository.save(cliente);
    }

    @Transactional
    public Cliente updateClient(UpdateDtoClient clientDto) throws ClientNotFoundException {
        return clienteRepository.findByIdAndEliminatoFalse(clientDto.getId())
                .map(clientEntityFromDB -> {
                    Cliente clienteToBeUpdated = clienteMapper.updateClient(clientEntityFromDB, clientDto);
                    return clienteRepository.save(clienteToBeUpdated);
                })
                .orElseThrow(() -> new ClientNotFoundException(
                        String.format(CLIENT_NOT_FOUND_MESSAGE, clientDto.getId())
                ));
    }

    private void validateInput(CreateDtoClient clientDto) {
        if (clientDto == null) {
            throw new IllegalArgumentException("Client DTO cannot be null");
        }
    }

    private void validateFiscalCode(String fiscalCode) throws InvalidFiscalCodeException {
        if (StringUtils.isNotBlank(fiscalCode) && !FiscalCodeValidator.isValid(fiscalCode)) {
            throw new InvalidFiscalCodeException("Invalid fiscal code format");
        }
    }
}
