package com.rrhh.AutenticacionYRol.Infrastructure.mappers;

import com.rrhh.AutenticacionYRol.Domain.entities.Usuario;
import com.rrhh.AutenticacionYRol.Infrastructure.models.UsuarioModel;

public class UsuarioMapper {

    public static Usuario toDomain(UsuarioModel model) {
        if (model == null) return null;

        Usuario domain = new Usuario();
        domain.setId(model.getIdUsuario());
        domain.setNombreUsuario(model.getNombreUsuario());
        domain.setContrasenia(model.getContrasenia());
        domain.setIntentosFallidos(model.getIntentosFallidos());
        domain.setCuentaBloqueada(model.isCuentaBloqueada());
        domain.setFechaBloqueo(model.getFechaBloqueo());
        domain.setDosFAActivado(model.isDosfaActivado());
        domain.setSecret2FA(model.getSecret2fa());

        // Mapeo rápido para la demo: ID 1 = RRHH
        if (model.getIdRol() != null && model.getIdRol() == 1L) {
            domain.setNombreRol("RRHH");
        } else {
            domain.setNombreRol("EMPLEADO");
        }

        return domain;
    }

    public static UsuarioModel toModel(Usuario domain) {
        if (domain == null) return null;

        UsuarioModel model = new UsuarioModel();
        model.setIdUsuario(domain.getId());
        model.setNombreUsuario(domain.getNombreUsuario());
        model.setContrasenia(domain.getContrasenia());
        model.setIntentosFallidos(domain.getIntentosFallidos());
        model.setCuentaBloqueada(domain.isCuentaBloqueada());
        model.setFechaBloqueo(domain.getFechaBloqueo());
        model.setDosfaActivado(domain.isDosFAActivado());
        model.setSecret2fa(domain.getSecret2FA());
        
        // Asignamos el ID 1 por defecto para la demo
        model.setIdRol(1L); 
        
        return model;
    }
}
