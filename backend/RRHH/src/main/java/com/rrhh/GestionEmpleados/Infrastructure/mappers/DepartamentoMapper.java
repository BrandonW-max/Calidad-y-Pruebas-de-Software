package com.rrhh.GestionEmpleados.Infrastructure.mappers;

import com.rrhh.GestionEmpleados.Domain.entities.Departamento;
import com.rrhh.Shared.persistence.DepartamentoModel;

public class DepartamentoMapper {

    public static Departamento toDomain(DepartamentoModel model) {

        if (model == null) {
            return null;
        }

        Departamento domain = new Departamento();

        domain.setIdDpto(model.getIdDpto());
        domain.setNombre(model.getNombre());
        domain.setCodDpto(model.getCodDpto());
        domain.setUbicacion(model.getUbicacion());
        domain.setActivo(model.getActivo());

        return domain;
    }
}