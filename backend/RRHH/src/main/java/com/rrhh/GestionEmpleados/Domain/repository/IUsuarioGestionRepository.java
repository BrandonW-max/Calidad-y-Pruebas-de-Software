package com.rrhh.GestionEmpleados.Domain.repository;

import com.rrhh.GestionEmpleados.Domain.entities.UsuarioGestion;

public interface IUsuarioGestionRepository {

    UsuarioGestion save(UsuarioGestion usuario);

    UsuarioGestion findByEmpleadoId(Long idEmpleado);

    boolean existsByNombreUsuario(String nombreUsuario);

    boolean existsByCorreoInst(String correoInst);
}