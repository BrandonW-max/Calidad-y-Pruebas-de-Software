package com.rrhh.AutenticacionYRol.Infrastructure.mappers;

import com.rrhh.AutenticacionYRol.Domain.entities.Usuario;
import com.rrhh.Shared.persistence.UsuarioModel;

public class UsuarioMapper {


    public static Usuario toDomain(UsuarioModel model) {

        if (model == null) {
            return null;
        }

        Usuario domain = new Usuario();

        domain.setId(model.getIdUsuario());

        domain.setNombreUsuario(
                model.getNombreUsuario()
        );

        domain.setCorreoInst(
                model.getCorreoInst()
        );

        domain.setContrasenia(
                model.getContrasenia()
        );

        domain.setIntentosFallidos(
                model.getIntentosFallidos()
        );

        domain.setCuentaBloqueada(
                model.getCuentaBloqueada()
        );

        domain.setFechaBloqueo(
                model.getFechaBloqueo()
        );

        domain.setDosFAActivado(
                model.getDosfaActivo()
        );

        domain.setSecret2FA(
                model.getDosfaSecret()
        );

        // =========================
        // ROL
        // =========================

        if (model.getRol() != null) {
            domain.setNombreRol(
                    model.getRol().getNombreRol()
            );
        }

        return domain;
    }

    // =========================
    // DOMAIN -> MODEL
    // =========================
    public static UsuarioModel toModel(Usuario domain) {

        if (domain == null) {
            return null;
        }

        UsuarioModel model = new UsuarioModel();

        model.setIdUsuario(
                domain.getId()
        );

        model.setNombreUsuario(
                domain.getNombreUsuario()
        );

        model.setCorreoInst(
                domain.getCorreoInst()
        );

        model.setContrasenia(
                domain.getContrasenia()
        );

        model.setIntentosFallidos(
                domain.getIntentosFallidos()
        );

        model.setCuentaBloqueada(
                domain.isCuentaBloqueada()
        );

        model.setFechaBloqueo(
                domain.getFechaBloqueo()
        );

        model.setDosfaActivo(
                domain.isDosFAActivado()
        );

        model.setDosfaSecret(
                domain.getSecret2FA()
        );

        /*
         * IMPORTANTE:
         * No asignamos rol aquí.
         * Antes tenías rol.setIdRol(1), eso hacía que todos terminaran como RRHH.
         * El rol se asigna al crear el usuario desde Gestión de Empleados.
         */

        return model;
    }
}