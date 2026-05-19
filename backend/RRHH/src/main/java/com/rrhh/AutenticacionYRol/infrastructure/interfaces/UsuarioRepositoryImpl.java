package com.rrhh.AutenticacionYRol.Infrastructure.interfaces;

import com.rrhh.AutenticacionYRol.Domain.entities.Usuario;
import com.rrhh.AutenticacionYRol.Domain.repository.IUsuarioRepository;

import com.rrhh.AutenticacionYRol.Infrastructure.mappers.UsuarioMapper;
import com.rrhh.AutenticacionYRol.Infrastructure.models.UsuarioModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UsuarioRepositoryImpl implements IUsuarioRepository {

    @Autowired
    private JpaUsuarioRepository jpaRepository; 
    public Usuario findByUsername(String username) {
        UsuarioModel model = jpaRepository.findByNombreUsuario(username);
        return UsuarioMapper.toDomain(model); // Convertimos a Dominio para el Service
    }

    @Override
    public void save(Usuario usuario) {
        UsuarioModel model = UsuarioMapper.toModel(usuario); // Convertimos a Modelo para Hibernate
        jpaRepository.save(model);
    }
}

