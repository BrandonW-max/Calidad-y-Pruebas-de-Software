package com.rrhh.Modulos.CU4_RegistroAsistencia.Domain.repository;

import com.rrhh.Modulos.CU4_RegistroAsistencia.Domain.entities.RegistroAsistencia;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Infrastructure.factory.RegistroAsistenciaFactory;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Infrastructure.interfaces.JpaRegistroAsistenciaNominaRepository;
import com.rrhh.Shared.persistence.RegistroAsistenciaModel;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public class RegistroAsistenciaRepositoryImpl implements IRegistroAsistenciaRepository {

    private final JpaRegistroAsistenciaNominaRepository jpaRegistroAsistenciaRepository;

    public RegistroAsistenciaRepositoryImpl(JpaRegistroAsistenciaNominaRepository jpaRegistroAsistenciaRepository) {
        this.jpaRegistroAsistenciaRepository = jpaRegistroAsistenciaRepository;
    }

    @Override
    public RegistroAsistencia guardar(RegistroAsistencia registroAsistencia) {
        RegistroAsistenciaModel model = RegistroAsistenciaFactory.crearModeloDesdeDominio(registroAsistencia);
        RegistroAsistenciaModel guardado = jpaRegistroAsistenciaRepository.save(model);
        return RegistroAsistenciaFactory.crearDominioDesdeModelo(guardado);
    }

    @Override
    public Optional<RegistroAsistencia> buscarPorEmpleadoYFecha(Integer idEmpleado, LocalDate fecha) {
        return jpaRegistroAsistenciaRepository.findByEmpleado_IdEmpleadoAndFecha(idEmpleado, fecha)
                .map(RegistroAsistenciaFactory::crearDominioDesdeModelo);
    }

    @Override
    public Optional<RegistroAsistencia> buscarPorId(Integer idRegistro) {
        return jpaRegistroAsistenciaRepository.findById(idRegistro)
                .map(RegistroAsistenciaFactory::crearDominioDesdeModelo);
    }

    // El mapeo se delega a la fábrica para aplicar el patrón Factory.
    // Las conversiones pueden cambiar sin afectar el repositorio.
}