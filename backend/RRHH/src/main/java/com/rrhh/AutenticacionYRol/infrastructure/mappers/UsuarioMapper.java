package com.rrhh.AutenticacionYRol.Infrastructure.mappers;

import com.rrhh.AutenticacionYRol.Domain.entities.Usuario;

import com.rrhh.Shared.persistence.UsuarioModel;
import com.rrhh.Shared.persistence.RolModel;

public class UsuarioMapper {

    // =========================
    // MODEL -> DOMAIN
    // =========================
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

        // =========================
        // ROL
        // =========================

        RolModel rol = new RolModel();

        rol.setIdRol(1);

        rol.setNombreRol(
                domain.getNombreRol() != null
                        ? domain.getNombreRol()
                        : "RRHH"
        );

        model.setRol(rol);

        return model;
    }
}