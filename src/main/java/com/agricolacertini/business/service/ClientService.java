package com.agricolacertini.business.service;

import com.agricolacertini.application.model.api.PageInfo;
import com.agricolacertini.application.model.api.clienti.CreateDtoClient;
import com.agricolacertini.application.model.api.clienti.SearchClientResponse;
import com.agricolacertini.application.model.api.clienti.SearchData;
import com.agricolacertini.application.model.api.clienti.UpdateDtoClient;
import com.agricolacertini.business.exception.CertiniClientNotFoundException;
import com.agricolacertini.business.exception.CertiniInvalidFiscalCodeException;
import com.agricolacertini.business.mapper.ClienteMapper;
import com.agricolacertini.business.specification.ClientSpecification;
import com.agricolacertini.business.util.FiscalCodeValidator;
import com.agricolacertini.persistence.entity.Cliente;
import com.agricolacertini.persistence.repository.ClienteRepository;
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
    public void markAsDeleted(Long userId) throws CertiniClientNotFoundException {
        clienteRepository.findByIdAndEliminatoFalse(userId)
                .map(client -> {
                    client.setEliminato(true);
                    return clienteRepository.save(client);
                })
                .orElseThrow(() -> new CertiniClientNotFoundException(
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
    public Cliente save(CreateDtoClient clientDto) throws CertiniInvalidFiscalCodeException {
        validateInput(clientDto);
        validateFiscalCode(clientDto.getCodiceFiscale());

        Cliente cliente = clienteMapper.clientDtoToClientEntity(clientDto);
        return clienteRepository.save(cliente);
    }

    @Transactional
    public Cliente updateClient(UpdateDtoClient clientDto) throws CertiniClientNotFoundException {
        return clienteRepository.findByIdAndEliminatoFalse(clientDto.getId())
                .map(clientEntityFromDB -> {
                    Cliente clienteToBeUpdated = clienteMapper.updateClient(clientEntityFromDB, clientDto);
                    return clienteRepository.save(clienteToBeUpdated);
                })
                .orElseThrow(() -> new CertiniClientNotFoundException(
                        String.format(CLIENT_NOT_FOUND_MESSAGE, clientDto.getId())
                ));
    }

    private void validateInput(CreateDtoClient clientDto) {
        if (clientDto == null) {
            throw new IllegalArgumentException("Client DTO cannot be null");
        }
    }

    private void validateFiscalCode(String fiscalCode) throws CertiniInvalidFiscalCodeException {
        if (StringUtils.isNotBlank(fiscalCode) && !FiscalCodeValidator.isValid(fiscalCode)) {
            throw new CertiniInvalidFiscalCodeException("Invalid fiscal code format");
        }
    }
}
