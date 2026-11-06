package com.rrhh.Modulos.CU3_Nomina.Infrastructure.interfaces;

import com.rrhh.Shared.persistence.NominaVersionAnteriorModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaNominaVersionAnteriorRepository extends JpaRepository<NominaVersionAnteriorModel, Integer> {

    Optional<NominaVersionAnteriorModel> findByIdEmpleadoAndPeriodo(Long idEmpleado, String periodo);
}
