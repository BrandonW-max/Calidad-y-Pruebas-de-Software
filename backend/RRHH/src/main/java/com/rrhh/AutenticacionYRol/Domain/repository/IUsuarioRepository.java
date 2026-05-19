package com.rrhh.AutenticacionYRol.Domain.repository;

import com.rrhh.AutenticacionYRol.Domain.entities.Usuario;

// 🛑 ELIMINA EL "extends JpaRepository" DE AQUÍ
public interface IUsuarioRepository {
    Usuario findByUsername(String username);
    void save(Usuario usuario);
}
