package com.rrhh.GestionEmpleados.Infrastructure.interfaces;

import com.rrhh.GestionEmpleados.Domain.entities.Departamento;
import com.rrhh.GestionEmpleados.Domain.repository.IDepartamentoRepository;
import com.rrhh.GestionEmpleados.Infrastructure.mappers.DepartamentoMapper;

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