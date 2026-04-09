package com.agricolacertini.application.model.api.clienti;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchData {

    private String nome;
    private String cognome;
    private String indirizzo;
}
