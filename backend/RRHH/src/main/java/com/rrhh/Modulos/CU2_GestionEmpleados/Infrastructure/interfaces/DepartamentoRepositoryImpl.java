package com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces;

import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Departamento;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IDepartamentoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.mappers.DepartamentoMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DepartamentoRepositoryImpl implements IDepartamentoRepository {

    @Autowired
    private JpaDepartamentoRepository jpaRepository;

    @Override
    public Departamento findById(Integer idDpto) {

        return jpaRepository.findById(idDpto)
                .map(DepartamentoMapper::toDomain)
                .orElse(null);
    }
}