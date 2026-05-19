package com.rrhh.AutenticacionYRol.Infrastructure.interfaces; // Revisa que el package sea igual a tus otros Impl

import com.rrhh.AutenticacionYRol.Domain.entities.Historial;
import com.rrhh.AutenticacionYRol.Domain.repository.IHistorialRepository;
import com.rrhh.AutenticacionYRol.Infrastructure.database.HistorialModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component // 💡 Esto es lo que le dice a Spring: "Yo soy el bean que buscas"
public class HistorialRepositoryImpl implements IHistorialRepository {

    @Autowired
    private JpaHistorialRepository jpaRepository;

    @Override
    public void save(Historial historial) {
        // Mapeo manual rápido para tu presentación
        HistorialModel model = new HistorialModel();
        model.setIdUsuario(historial.getIdUsuario());
        model.setAccion(historial.getAccion());
        model.setIpMaquina(historial.getIpMaquina());
        model.setTablaAfectada(historial.getTablaAfectada());
        model.setFechaCambio(LocalDateTime.now());
        
        jpaRepository.save(model);
    }
}
