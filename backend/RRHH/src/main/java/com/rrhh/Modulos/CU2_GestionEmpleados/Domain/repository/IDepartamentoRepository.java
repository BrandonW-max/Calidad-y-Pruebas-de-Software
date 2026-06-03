package com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository;

import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Departamento;

public interface IDepartamentoRepository {

    Departamento findById(Integer idDpto);
}