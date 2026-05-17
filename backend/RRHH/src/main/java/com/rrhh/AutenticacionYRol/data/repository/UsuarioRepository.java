package com.rrhh.AutenticacionYRol.data.repository;

import com.rrhh.AutenticacionYRol.data.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Usuario findByUsername(String username);
}