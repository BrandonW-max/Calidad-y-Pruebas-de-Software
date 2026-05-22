package com.rrhh.AutenticacionYRol.Infrastructure.interfaces;

import com.rrhh.AutenticacionYRol.Domain.entities.Usuario;
import com.rrhh.AutenticacionYRol.Domain.repository.IUsuarioRepository;

import com.rrhh.AutenticacionYRol.Infrastructure.mappers.UsuarioMapper;

import com.rrhh.Shared.persistence.UsuarioModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UsuarioRepositoryImpl implements IUsuarioRepository {

    @Autowired
    private JpaUsuarioRepository jpaRepository;

    @Override
    public Usuario findByUsername(String username) {

        return jpaRepository.findByNombreUsuario(username)
                .map(UsuarioMapper::toDomain)
                .orElse(null);
    }

    @Override
    public Usuario findByNombreUsuarioOrCorreoInst(
        String nombreUsuario,
        String correoInst
    ) {

    return jpaRepository
            .findByNombreUsuarioOrCorreoInst(
                    nombreUsuario,
                    correoInst
            )
            .map(UsuarioMapper::toDomain)
            .orElse(null);
    }

    @Override
    public Usuario save(Usuario usuario) {

        UsuarioModel model =
                UsuarioMapper.toModel(usuario);

        UsuarioModel saved =
                jpaRepository.save(model);

        return UsuarioMapper.toDomain(saved);
    }
}