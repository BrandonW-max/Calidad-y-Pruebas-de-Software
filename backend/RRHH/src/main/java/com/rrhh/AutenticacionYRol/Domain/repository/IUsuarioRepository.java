package com.rrhh.AutenticacionYRol.Domain.repository;

import com.rrhh.AutenticacionYRol.Domain.entities.Usuario;

public interface IUsuarioRepository {

    Usuario findByUsername(String username);

    Usuario findByNombreUsuarioOrCorreoInst(
            String nombreUsuario,
            String correoInst
    );

    Usuario save(Usuario usuario);
}
