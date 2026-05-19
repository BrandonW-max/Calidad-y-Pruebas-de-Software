package com.rrhh.AutenticacionYRol.Infrastructure.interfaces;

import com.rrhh.AutenticacionYRol.Infrastructure.database.HistorialModel; // <--- Ahora sí existirá
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaHistorialRepository extends JpaRepository<HistorialModel, Long> {
}
