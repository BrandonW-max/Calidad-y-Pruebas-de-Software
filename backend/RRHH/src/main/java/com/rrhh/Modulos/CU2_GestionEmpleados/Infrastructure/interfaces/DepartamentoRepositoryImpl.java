package com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces;

import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Departamento;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IDepartamentoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.mappers.DepartamentoMapper;

import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class DepartamentoRepositoryImpl implements IDepartamentoRepository {

    private final JpaDepartamentoRepository jpaRepository;

    public DepartamentoRepositoryImpl(JpaDepartamentoRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Departamento findById(Integer idDpto) {

        return jpaRepository.findById(Objects.requireNonNull(idDpto))
                .map(DepartamentoMapper::toDomain)
                .orElse(null);
    }
}