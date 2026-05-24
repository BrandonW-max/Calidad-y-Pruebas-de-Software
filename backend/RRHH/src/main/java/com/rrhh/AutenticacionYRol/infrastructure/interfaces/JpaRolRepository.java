package com.rrhh.AutenticacionYRol.Infrastructure.interfaces;

import com.rrhh.Shared.persistence.RolModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaRolRepository extends JpaRepository<RolModel, Integer> {

    Optional<RolModel> findByNombreRol(String nombreRol);
}