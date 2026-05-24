package com.rrhh.GestionEmpleados.Infrastructure.interfaces;

import com.rrhh.GestionEmpleados.Domain.entities.Empleado;
import com.rrhh.GestionEmpleados.Domain.repository.IEmpleadoRepository;
import com.rrhh.GestionEmpleados.Infrastructure.mappers.EmpleadoMapper;
import com.rrhh.Shared.persistence.EmpleadoModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class EmpleadoRepositoryImpl implements IEmpleadoRepository {

    @Autowired
    private JpaEmpleadoRepository jpaRepository;

    @Override
    public Empleado save(Empleado empleado) {

        EmpleadoModel model =
                EmpleadoMapper.toModel(empleado);

        EmpleadoModel saved =
                jpaRepository.save(model);

        return EmpleadoMapper.toDomain(saved);
    }

    @Override
    public Empleado findById(Long idEmpleado) {

        return jpaRepository.findById(idEmpleado)
                .map(EmpleadoMapper::toDomain)
                .orElse(null);
    }

    @Override
    public boolean existsByNumeroDi(String numeroDi) {
        return jpaRepository.existsByNumeroDi(numeroDi);
    }

    @Override
    public List<Empleado> findAll() {

        List<EmpleadoModel> models =
                jpaRepository.findAll();

        List<Empleado> empleados =
                new ArrayList<>();

        for (EmpleadoModel model : models) {
            empleados.add(
                    EmpleadoMapper.toDomain(model)
            );
        }

        return empleados;
    }

    @Override
    public List<Empleado> buscarPorFiltro(String filtro) {

        List<EmpleadoModel> models =
                jpaRepository
                        .findByNumeroDiContainingIgnoreCaseOrNombresContainingIgnoreCaseOrApellidosContainingIgnoreCase(
                                filtro,
                                filtro,
                                filtro
                        );

        List<Empleado> empleados =
                new ArrayList<>();

        for (EmpleadoModel model : models) {
            empleados.add(
                    EmpleadoMapper.toDomain(model)
            );
        }

        return empleados;
    }
    @Override
public List<Empleado> buscarAvanzado(
        String estado,
        Integer idDpto,
        String cargo
) {

    List<EmpleadoModel> models =
            jpaRepository.buscarAvanzado(
                    estado,
                    idDpto,
                    cargo
            );

    List<Empleado> empleados =
            new ArrayList<>();

    for (EmpleadoModel model : models) {
        empleados.add(
                EmpleadoMapper.toDomain(model)
        );
    }

    return empleados;
}
}