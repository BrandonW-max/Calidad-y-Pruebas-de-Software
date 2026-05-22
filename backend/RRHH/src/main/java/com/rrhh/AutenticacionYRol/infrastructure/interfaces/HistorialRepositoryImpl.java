package com.rrhh.AutenticacionYRol.Infrastructure.interfaces;

import com.rrhh.AutenticacionYRol.Domain.entities.Historial;
import com.rrhh.AutenticacionYRol.Domain.repository.IHistorialRepository;
import com.rrhh.Shared.persistence.HistorialAuditoriaModel;
import com.rrhh.Shared.persistence.UsuarioModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class HistorialRepositoryImpl implements IHistorialRepository {

    @Autowired
    private JpaHistorialRepository jpaRepository;

    @Override
    public void save(Historial historial) {

        HistorialAuditoriaModel model = new HistorialAuditoriaModel();

        // =========================
        // USUARIO
        // =========================
        UsuarioModel usuario = new UsuarioModel();
        usuario.setIdUsuario(historial.getIdUsuario());

        model.setUsuario(usuario);

        // =========================
        // DATOS AUDITORIA
        // =========================
        model.setAccion(historial.getAccion());
        model.setIpMaquina(historial.getIpMaquina());
        model.setTablaAfectada(historial.getTablaAfectada());

        // Registro afectado: en login es el mismo usuario
        model.setIdRegistroAfectado(historial.getIdUsuario());

        model.setFechaDeCambio(LocalDateTime.now());

        jpaRepository.save(model);
    }
}