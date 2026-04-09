package com.poultryfarm.application.rest;

import com.poultryfarm.application.model.PageInfo;
import com.poultryfarm.application.model.CreateDtoClient;
import com.poultryfarm.application.model.SearchClientResponse;
import com.poultryfarm.application.model.SearchData;
import com.poultryfarm.application.model.UpdateDtoClient;
import com.poultryfarm.business.exception.CertiniClientNotFoundException;
import com.poultryfarm.business.exception.CertiniInvalidFiscalCodeException;
import com.poultryfarm.business.service.ClientService;
import com.poultryfarm.persistence.entity.Cliente;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/clienti")
public class ClienteRestController extends BaseRestController {

    private final ClientService clientService;

    @Autowired
    public ClienteRestController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> findById(@PathVariable("id") Long id) {
        final Optional<Cliente> clientOptional = this.clientService.findById(id);
        return clientOptional.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/ricerca")
    public ResponseEntity<SearchClientResponse> searchClients(@RequestParam(required = false) Integer pageNumber,
                                                               @RequestParam(required = false) Integer pageSize,
                                                               @RequestParam(required = false, defaultValue = "false") boolean isPaginated,
                                                               @RequestBody SearchData searchData) {
        SearchClientResponse body;
        if (isPaginated) {
            body = this.clientService.findAll(PageInfo
                    .builder()
                    .pageNumber(this.getPageNumber(pageNumber))
                    .pageSize(this.getPageSize(pageSize))
                    .build(), searchData, true);
        } else {
            body = this.clientService.findAll(null, searchData, false);
        }

        return ResponseEntity.ok(body);
    }

    @PostMapping
    public ResponseEntity<Cliente> createClient(@RequestBody @Valid CreateDtoClient clientDto) {
        try {
            Cliente newClient = this.clientService.save(clientDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(newClient);
        } catch (CertiniInvalidFiscalCodeException certiniInvalidFiscalCodeException) {
            return handleInvalidFiscalCode(certiniInvalidFiscalCodeException);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClientById(@PathVariable("id") Long id) {
        try {
            this.clientService.markAsDeleted(id);
            return ResponseEntity.noContent().build();
        } catch (CertiniClientNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping
    public ResponseEntity<Cliente> updateClient(@RequestBody UpdateDtoClient clientDto) {
        try {
            this.clientService.updateClient(clientDto);
            return ResponseEntity.ok(this.clientService.updateClient(clientDto));
        } catch (CertiniClientNotFoundException certiniClientNotFoundException) {
            return ResponseEntity.notFound().build();
        }
    }

    private ResponseEntity<Cliente> handleInvalidFiscalCode(CertiniInvalidFiscalCodeException e) {
        return ResponseEntity
                .badRequest()
                .header("X-Error-Message", e.getMessage())
                .build();
    }
}
