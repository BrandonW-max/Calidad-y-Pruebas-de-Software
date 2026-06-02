package com.rrhh.GestionEmpleados.Infrastructure.interfaces;

import com.rrhh.AutenticacionYRol.Infrastructure.interfaces.JpaUsuarioRepository;
import com.rrhh.GestionEmpleados.Domain.entities.UsuarioGestion;
import com.rrhh.GestionEmpleados.Domain.repository.IUsuarioGestionRepository;
import com.rrhh.GestionEmpleados.Infrastructure.mappers.UsuarioGestionMapper;
import com.rrhh.Shared.persistence.UsuarioModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UsuarioGestionRepositoryImpl implements IUsuarioGestionRepository {

    @Autowired
    private JpaUsuarioRepository jpaRepository;

    @Override
    public UsuarioGestion save(UsuarioGestion usuario) {

        UsuarioModel model =
                UsuarioGestionMapper.toModel(usuario);

        UsuarioModel saved =
                jpaRepository.save(model);

        return UsuarioGestionMapper.toDomain(saved);
    }

    @Override
    public UsuarioGestion findByEmpleadoId(Long idEmpleado) {

        return jpaRepository.findByEmpleadoIdEmpleado(idEmpleado)
                .map(UsuarioGestionMapper::toDomain)
                .orElse(null);
    }

    @Override
    public boolean existsByNombreUsuario(String nombreUsuario) {
        return jpaRepository.existsByNombreUsuario(nombreUsuario);
    }

    @Override
    public boolean existsByCorreoInst(String correoInst) {
        return jpaRepository.existsByCorreoInst(correoInst);
    }
}