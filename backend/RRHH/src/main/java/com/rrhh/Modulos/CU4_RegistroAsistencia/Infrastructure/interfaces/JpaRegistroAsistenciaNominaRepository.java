package com.rrhh.Modulos.CU4_RegistroAsistencia.Infrastructure.interfaces;

import com.rrhh.Shared.persistence.RegistroAsistenciaModel;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface JpaRegistroAsistenciaNominaRepository extends JpaRepository<RegistroAsistenciaModel, Integer> {

    Optional<RegistroAsistenciaModel> findByEmpleado_IdEmpleadoAndFechaAndTipoRegistro(Integer idEmpleado, LocalDate fecha, String tipoRegistro);

    // Bloquea el registro del día mientras se marca: dos marcaciones simultáneas no pueden registrar la misma salida
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<RegistroAsistenciaModel> findConBloqueoByEmpleado_IdEmpleadoAndFechaAndTipoRegistro(Integer idEmpleado, LocalDate fecha, String tipoRegistro);

    // Reclama la salida solo si aún está vacía: devuelve 1 si esta marcación la registró, 0 si otra ya lo hizo
    @Modifying
    @Query(value = "UPDATE registro_asistencia SET hora_salida = :horaSalida WHERE id_registro = :idRegistro AND hora_salida IS NULL", nativeQuery = true)
    int reclamarSalida(@Param("idRegistro") Integer idRegistro, @Param("horaSalida") LocalTime horaSalida);

    List<RegistroAsistenciaModel> findByEmpleado_IdEmpleadoAndFecha(Integer idEmpleado, LocalDate fecha);

    List<RegistroAsistenciaModel> findByFechaAndHoraSalidaIsNullAndTipoRegistro(LocalDate fecha, String tipoRegistro);

    List<RegistroAsistenciaModel> findByEmpleado_IdEmpleadoOrderByFechaDesc(Integer idEmpleado);
}