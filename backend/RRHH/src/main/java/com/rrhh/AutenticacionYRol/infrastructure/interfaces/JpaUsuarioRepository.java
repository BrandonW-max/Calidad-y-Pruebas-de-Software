package com.rrhh.AutenticacionYRol.infrastructure.interfaces;

import com.rrhh.Shared.persistence.UsuarioModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

import java.util.Optional;

@Repository
public interface JpaUsuarioRepository extends JpaRepository<UsuarioModel, Long> {

    Optional<UsuarioModel> findByNombreUsuario(String nombreUsuario);

    Optional<UsuarioModel> findByNombreUsuarioOrCorreoInst(
        String nombreUsuario,
        String correoInst
    );
    boolean existsByNombreUsuario(String nombreUsuario);

    boolean existsByCorreoInst(String correoInst);

    Optional<UsuarioModel> findByEmpleadoIdEmpleado(Long idEmpleado);
    Optional<UsuarioModel> findByCorreoInst(String correoInst);
    @Query("""
        SELECT u
        FROM UsuarioModel u
        JOIN u.empleado e
        WHERE LOWER(e.correo) = LOWER(:correo)
           OR LOWER(u.correoInst) = LOWER(:correo)
        """)
Optional<UsuarioModel> findByCorreoPersonalOInstitucional(
        @Param("correo") String correo
);
}