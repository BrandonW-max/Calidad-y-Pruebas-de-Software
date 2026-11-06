package com.rrhh.Modulos.CU3_Nomina.Infrastructure.interfaces;

import com.rrhh.Shared.persistence.NominaVersionModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaNominaVersionRepository extends JpaRepository<NominaVersionModel, Integer> {

    List<NominaVersionModel> findByIdNominaOrderByVersionDesc(Integer idNomina);
}
