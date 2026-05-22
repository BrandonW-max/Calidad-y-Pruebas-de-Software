package com.rrhh.AutenticacionYRol.Infrastructure.interfaces;

import com.rrhh.Shared.persistence.UsuarioModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaUsuarioRepository extends JpaRepository<UsuarioModel, Long> {

    Optional<UsuarioModel> findByNombreUsuario(String nombreUsuario);

    Optional<UsuarioModel> findByNombreUsuarioOrCorreoInst(
        String nombreUsuario,
        String correoInst
    );
}