package com.rrhh.AutenticacionYRol.Infrastructure.interfaces;

import com.rrhh.Shared.persistence.HistorialAuditoriaModel; 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaHistorialRepository extends JpaRepository<HistorialAuditoriaModel, Long> {
}
