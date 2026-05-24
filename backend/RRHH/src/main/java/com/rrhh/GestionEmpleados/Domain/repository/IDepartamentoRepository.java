package com.rrhh.GestionEmpleados.Domain.repository;

import com.rrhh.GestionEmpleados.Domain.entities.Departamento;

public interface IDepartamentoRepository {

    Departamento findById(Integer idDpto);
}