package com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces;

import com.rrhh.Shared.persistence.ContratoModel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JpaContratoRepository extends JpaRepository<ContratoModel, Long> {

    Optional<ContratoModel> findByEmpleadoIdEmpleadoAndEstado(
            Long idEmpleado,
            String estado
    );

    Optional<ContratoModel> findFirstByEmpleadoIdEmpleadoAndEstado(Long idEmpleado, String estado);

    Optional<ContratoModel> findFirstByEmpleadoIdEmpleadoAndEstadoOrderByIdContratoDesc(Long idEmpleado, String estado);

    List<ContratoModel> findByEmpleadoIdEmpleadoInAndEstado(List<Long> idsEmpleados, String estado);

    // Cargos de contratos activos; departamento vacío = todos los departamentos
    @Query("SELECT DISTINCT c.cargo FROM ContratoModel c " +
           "WHERE c.estado = 'ACTIVO' AND c.cargo IS NOT NULL AND " +
           "(:departamento = '' OR c.departamento.nombre = :departamento) " +
           "ORDER BY c.cargo")
    List<String> buscarCargosActivos(@Param("departamento") String departamento);
}