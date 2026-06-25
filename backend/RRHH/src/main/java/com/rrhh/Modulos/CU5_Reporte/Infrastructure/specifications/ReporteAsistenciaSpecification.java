package com.rrhh.Modulos.CU5_Reporte.Infrastructure.specifications;

import com.rrhh.Shared.persistence.ContratoModel;
import com.rrhh.Shared.persistence.EmpleadoModel;
import com.rrhh.Shared.persistence.RegistroAsistenciaModel;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReporteAsistenciaSpecification {

    public static Specification<RegistroAsistenciaModel> conFiltros(
            Long idEmpleado, String area,
            LocalDate fechaInicio, LocalDate fechaFin) {

        return (root, query, cb) -> {
            query.distinct(true);

            Join<RegistroAsistenciaModel, EmpleadoModel> emp = root.join("empleado");
            Join<EmpleadoModel, ContratoModel> ctr = emp.join("contratos", JoinType.INNER);

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(ctr.get("estado"), "ACTIVO"));

            if (idEmpleado != null) {
                predicates.add(cb.equal(root.get("empleado").get("idEmpleado"), idEmpleado));
            }
            if (area != null) {
                predicates.add(cb.equal(ctr.get("departamento").get("nombre"), area));
            }
            if (fechaInicio != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fecha"), fechaInicio));
            }
            if (fechaFin != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fecha"), fechaFin));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
