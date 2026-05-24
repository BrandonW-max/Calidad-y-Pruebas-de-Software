package com.rrhh.GestionEmpleados.Domain.repository;

import com.rrhh.GestionEmpleados.Domain.entities.Contrato;

public interface IContratoRepository {

    Contrato save(Contrato contrato);

    Contrato findContratoActivoByEmpleado(Long idEmpleado);
}