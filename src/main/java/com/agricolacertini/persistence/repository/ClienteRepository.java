package com.agricolacertini.persistence.repository;

import com.agricolacertini.persistence.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long>, JpaSpecificationExecutor<Cliente> {

    /**
     * Retrieves a client entity by its unique identifier, where the client is not marked as deleted.
     */
    Optional<Cliente> findByIdAndEliminatoFalse(Long id);
}
