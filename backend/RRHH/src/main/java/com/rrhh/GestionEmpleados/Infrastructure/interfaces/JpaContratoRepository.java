package com.rrhh.GestionEmpleados.Infrastructure.interfaces;

import com.rrhh.Shared.persistence.ContratoModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaContratoRepository extends JpaRepository<ContratoModel, Long> {

    Optional<ContratoModel> findByEmpleadoIdEmpleadoAndEstado(
            Long idEmpleado,
            String estado
    );
}