package com.rrhh.GestionEmpleados.Domain.repository;

import com.rrhh.GestionEmpleados.Domain.entities.Empleado;

import java.util.List;

public interface IEmpleadoRepository {

    Empleado save(Empleado empleado);

    Empleado findById(Long idEmpleado);

    boolean existsByNumeroDi(String numeroDi);

    List<Empleado> findAll();

    List<Empleado> buscarPorFiltro(String filtro);

    List<Empleado> buscarAvanzado(
            String estado,
            Integer idDpto,
            String cargo
    );
}