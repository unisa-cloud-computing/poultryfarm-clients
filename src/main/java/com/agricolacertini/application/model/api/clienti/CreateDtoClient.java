package com.agricolacertini.application.model.api.clienti;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
public class CreateDtoClient {

    @NotBlank(message = "Il nome è obbligatorio")
    private String nome;

    @NotBlank(message = "Il cognome è obbligatorio")
    private String cognome;

    @NotBlank(message = "Il cellulare è obbligatorio")
    @Pattern(
            regexp = "^(\\+39)?3\\d{9}$",
            message = "Il numero di cellulare deve essere nel formato +393XXXXXXXX oppure 3XXXXXXXXX"
    )
    private String cellulare;

    private String codiceFiscale;

    @Email(message = "Formato email non valido")
    private String email;

    @NotBlank(message = "L'indirizzo è obbligatorio")
    private String indirizzo;

    @NotBlank(message = "La provincia è obbligatoria")
    private String provincia;

    @NotBlank(message = "Il comune è obbligatorio")
    private String comune;

    private String codiceIdentificativoAsl;

    private LocalDate dataNascita;
}
