package com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.interfaces;

import com.rrhh.Shared.persistence.HistorialAuditoriaModel; 
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaHistorialRepository extends JpaRepository<HistorialAuditoriaModel, Long> {
}
