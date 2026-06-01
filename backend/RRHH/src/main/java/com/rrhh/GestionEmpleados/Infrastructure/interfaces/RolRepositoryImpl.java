package com.rrhh.GestionEmpleados.Infrastructure.interfaces;

import com.rrhh.AutenticacionYRol.infrastructure.interfaces.JpaRolRepository;
import com.rrhh.GestionEmpleados.Domain.entities.Rol;
import com.rrhh.GestionEmpleados.Domain.repository.IRolRepository;
import com.rrhh.GestionEmpleados.Infrastructure.mappers.RolGestionMapper;

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