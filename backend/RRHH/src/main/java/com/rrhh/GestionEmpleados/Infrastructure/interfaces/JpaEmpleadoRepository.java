package com.rrhh.GestionEmpleados.Infrastructure.interfaces;

import com.rrhh.Shared.persistence.EmpleadoModel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaEmpleadoRepository extends JpaRepository<EmpleadoModel, Long> {

    boolean existsByNumeroDi(String numeroDi);

    List<EmpleadoModel> findByEstado(String estado);

    List<EmpleadoModel> findByNumeroDiContainingIgnoreCaseOrNombresContainingIgnoreCaseOrApellidosContainingIgnoreCase(
            String numeroDi,
            String nombres,
            String apellidos
    );

    @Query(value = """
            SELECT DISTINCT e.*
            FROM empleado e
            LEFT JOIN contrato c ON e.id_empleado = c.id_empleado
            LEFT JOIN departamento d ON d.id_dpto = c.id_dpto
            WHERE (:estado IS NULL OR e.estado = CAST(:estado AS TEXT))
              AND (:idDpto IS NULL OR d.id_dpto = :idDpto)
              AND (
                    :cargo IS NULL 
                    OR LOWER(c.cargo) LIKE LOWER(CONCAT('%', CAST(:cargo AS TEXT), '%'))
                  )
              AND (c.estado = 'ACTIVO' OR c.estado IS NULL)
            """, nativeQuery = true)
    List<EmpleadoModel> buscarAvanzado(
            @Param("estado") String estado,
            @Param("idDpto") Integer idDpto,
            @Param("cargo") String cargo
    );
}