package com.poultryfarm.business.specification;

import com.poultryfarm.application.model.SearchData;
import com.poultryfarm.persistence.entity.Cliente;
import io.micrometer.common.util.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ClientSpecification {

    protected Specification<Cliente> getNomeLikeSpec(String nome) {
        return ((root, query, builder) -> builder.like(builder.lower(root.get("nome")), "%" + nome.toLowerCase() + "%"));
    }

    protected Specification<Cliente> getCognomeLikeSpec(String cognome) {
        return ((root, query, builder) -> builder.like(builder.lower(root.get("cognome")), "%" + cognome.toLowerCase() + "%"));
    }

    protected Specification<Cliente> getIndirizzoLikeSpec(String indirizzo) {
        return ((root, query, builder) -> builder.like(builder.lower(root.get("indirizzo")), "%" + indirizzo.toLowerCase() + "%"));
    }

    public Specification<Cliente> getNomeCognomeIndirizzoLikeSpecs(SearchData searchData) {
        Specification<Cliente> filters = Specification.where(this.getClientiNonEliminati());

        if (!Objects.isNull(searchData.getNome()) && StringUtils.isNotBlank(searchData.getNome()))
            filters = filters.and(this.getNomeLikeSpec(searchData.getNome()));

        if (!Objects.isNull(searchData.getCognome()) && StringUtils.isNotBlank(searchData.getCognome()))
            filters = filters.and(this.getCognomeLikeSpec(searchData.getCognome()));

        if (!Objects.isNull(searchData.getIndirizzo()) && StringUtils.isNotBlank(searchData.getIndirizzo()))
            filters = filters.and(this.getIndirizzoLikeSpec(searchData.getIndirizzo()));

        return filters;
    }

    private Specification<Cliente> getClientiNonEliminati() {
        return ((root, query, builder) -> builder.isFalse(root.get("eliminato")));
    }
}
