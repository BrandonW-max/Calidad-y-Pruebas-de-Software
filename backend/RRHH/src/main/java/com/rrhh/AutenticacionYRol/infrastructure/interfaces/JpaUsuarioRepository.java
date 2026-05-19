package com.rrhh.AutenticacionYRol.Infrastructure.interfaces;

import com.rrhh.AutenticacionYRol.Infrastructure.models.UsuarioModel; // Tu modelo de base de datos
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaUsuarioRepository extends JpaRepository<UsuarioModel, Long> {
    UsuarioModel findByNombreUsuario(String nombreUsuario);
}
