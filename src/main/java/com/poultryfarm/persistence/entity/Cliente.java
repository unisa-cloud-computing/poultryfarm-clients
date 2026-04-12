package com.poultryfarm.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "CLIENTE", schema = "dbo")
@Data
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "NOME", nullable = false)
    private String nome;
    @Column(name = "COGNOME", nullable = false)
    private String cognome;
    @Column(name = "CELLULARE", nullable = false)
    private String cellulare;
    @Column(name = "TELEFONO")
    private String telefono;
    @Column(name = "CODICE_FISCALE", unique = true)
    private String codiceFiscale;
    @Column(name = "EMAIL")
    private String email;
    @Column(name = "INDIRIZZO", nullable = false)
    private String indirizzo;
    @Column(name = "PROV", nullable = false)
    private String provincia;
    @Column(name = "COMUNE", nullable = false)
    private String comune;
    @Column(name = "CODICE_IDENTIFICATIVO_ASL")
    private String codiceIdentificativoAsl;
    @Column(name = "DATA_DI_NASCITA")
    private LocalDate dataNascita;
    @Column(name = "ELIMINATO", nullable = false)
    private Boolean eliminato = false;

    public Cliente() {}

    public Cliente(String nome, String cognome, String indirizzo, String cellulare,
                   String telefono, String codiceIdentificativoAsl, String provincia, String comune) {
        this.nome = nome;
        this.cognome = cognome;
        this.indirizzo = indirizzo;
        this.cellulare = cellulare;
        this.telefono = telefono;
        this.codiceIdentificativoAsl = codiceIdentificativoAsl;
        this.provincia = provincia;
        this.comune = comune;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Cliente cliente = (Cliente) o;
        return Objects.equals(id, cliente.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
