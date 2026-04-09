package com.agricolacertini.application.model.api.clienti;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDtoClient {

    @NotNull(message = "L'id è obbligatorio per l'aggiornamento")
    private Long id;

    private String nome;
    private String cognome;
    @Pattern(
            regexp = "^(\\+39)?3\\d{9}$",
            message = "Il numero di cellulare deve essere nel formato +393XXXXXXXX oppure 3XXXXXXXXX"
    )
    private String cellulare;
    private String codiceFiscale;

    @Email(message = "Formato email non valido")
    private String email;

    private String indirizzo;
    private String provincia;
    private String comune;
    private String codiceIdentificativoAsl;
    private LocalDate dataNascita;
}
