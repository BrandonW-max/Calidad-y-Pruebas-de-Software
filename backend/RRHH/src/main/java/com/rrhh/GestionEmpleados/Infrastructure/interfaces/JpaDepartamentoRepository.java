package com.rrhh.GestionEmpleados.Infrastructure.interfaces;

import com.rrhh.Shared.persistence.DepartamentoModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaDepartamentoRepository extends JpaRepository<DepartamentoModel, Integer> {
}