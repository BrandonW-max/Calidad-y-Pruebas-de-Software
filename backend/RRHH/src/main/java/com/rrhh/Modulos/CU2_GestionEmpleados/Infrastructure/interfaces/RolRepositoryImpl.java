package com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.interfaces.JpaRolRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Rol;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IRolRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.mappers.RolGestionMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RolRepositoryImpl implements IRolRepository {

    @Autowired
    private JpaRolRepository jpaRepository;

    @Override
    public Rol findByNombreRol(String nombreRol) {

        return jpaRepository.findByNombreRol(nombreRol)
                .map(RolGestionMapper::toDomain)
                .orElse(null);
    }
}