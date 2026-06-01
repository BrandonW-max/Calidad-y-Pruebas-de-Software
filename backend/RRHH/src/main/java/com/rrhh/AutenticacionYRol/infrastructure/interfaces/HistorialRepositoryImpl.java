package com.rrhh.AutenticacionYRol.infrastructure.interfaces;

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
        // USUARIO QUE REALIZA LA ACCIÓN
        // =========================
        if (historial.getIdUsuario() != null) {
            UsuarioModel usuario = new UsuarioModel();
            usuario.setIdUsuario(historial.getIdUsuario());

            model.setUsuario(usuario);
        }

        // =========================
        // DATOS DE AUDITORÍA
        // =========================
        model.setAccion(historial.getAccion());
        model.setIpMaquina(historial.getIpMaquina());
        model.setTablaAfectada(historial.getTablaAfectada());

        model.setIdRegistroAfectado(
                historial.getIdRegistroAfectado()
        );

        model.setValorAnterior(
                historial.getValorAnterior()
        );

        model.setValorNuevo(
                historial.getValorNuevo()
        );

        model.setFechaDeCambio(
                historial.getFechaCambio() != null
                        ? historial.getFechaCambio()
                        : LocalDateTime.now()
        );

        model.setCreadoEl(
                LocalDateTime.now()
        );

        jpaRepository.save(model);
    }
}