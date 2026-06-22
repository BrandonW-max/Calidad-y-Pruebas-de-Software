package com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces;

import com.rrhh.Shared.persistence.EmpleadoDerechoHabientesModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaEmpleadoDerechoHabientesRepository extends JpaRepository<EmpleadoDerechoHabientesModel, Long> {
    Optional<EmpleadoDerechoHabientesModel> findByEmpleadoIdEmpleado(Long idEmpleado);
}
