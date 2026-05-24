package com.rrhh.GestionEmpleados.Infrastructure.interfaces;

import com.rrhh.GestionEmpleados.Domain.entities.Contrato;
import com.rrhh.GestionEmpleados.Domain.repository.IContratoRepository;
import com.rrhh.GestionEmpleados.Infrastructure.mappers.ContratoMapper;
import com.rrhh.Shared.persistence.ContratoModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ContratoRepositoryImpl implements IContratoRepository {

    @Autowired
    private JpaContratoRepository jpaRepository;

    @Override
    public Contrato save(Contrato contrato) {

        ContratoModel model =
                ContratoMapper.toModel(contrato);

        ContratoModel saved =
                jpaRepository.save(model);

        return ContratoMapper.toDomain(saved);
    }

    @Override
    public Contrato findContratoActivoByEmpleado(Long idEmpleado) {

        return jpaRepository
                .findByEmpleadoIdEmpleadoAndEstado(
                        idEmpleado,
                        "ACTIVO"
                )
                .map(ContratoMapper::toDomain)
                .orElse(null);
    }
}