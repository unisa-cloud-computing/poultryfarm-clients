package com.poultryfarm.application.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchClientsDto {
    private Long id;
    private String nome;
    private String cognome;
    private String indirizzo;
    private String comune;
    private String codiceIdentificativoAsl;
    private String provincia;
}
