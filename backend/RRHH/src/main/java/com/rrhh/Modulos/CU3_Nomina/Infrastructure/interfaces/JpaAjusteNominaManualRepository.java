package com.rrhh.Modulos.CU3_Nomina.Infrastructure.interfaces;

import com.rrhh.Shared.persistence.AjusteNominaManualModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaAjusteNominaManualRepository extends JpaRepository<AjusteNominaManualModel, Integer> {

    List<AjusteNominaManualModel> findByPeriodo(String periodo);

    Optional<AjusteNominaManualModel> findByIdEmpleadoAndPeriodo(Long idEmpleado, String periodo);
}
