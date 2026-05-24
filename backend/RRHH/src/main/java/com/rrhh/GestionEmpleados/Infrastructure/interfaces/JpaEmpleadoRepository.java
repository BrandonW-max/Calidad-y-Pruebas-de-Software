package com.rrhh.GestionEmpleados.Infrastructure.interfaces;

import com.rrhh.Shared.persistence.EmpleadoModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaEmpleadoRepository extends JpaRepository<EmpleadoModel, Long> {

    boolean existsByNumeroDi(String numeroDi);

    List<EmpleadoModel> findByEstado(String estado);

    List<EmpleadoModel> findByNombresContainingIgnoreCaseOrApellidosContainingIgnoreCase(
            String nombres,
            String apellidos
    );
}