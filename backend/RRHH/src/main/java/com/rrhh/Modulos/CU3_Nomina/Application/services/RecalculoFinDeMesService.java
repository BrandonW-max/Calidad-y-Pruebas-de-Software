package com.rrhh.Modulos.CU3_Nomina.Application.services;

import com.rrhh.Modulos.CU3_Nomina.Application.dto.CalcularNominaRequestDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;

@Service
public class RecalculoFinDeMesService {

    private static final Logger log = LoggerFactory.getLogger(RecalculoFinDeMesService.class);
    private static final Long ID_USUARIO_SISTEMA = 1L;

    private final INominaService nominaService;

    @PersistenceContext
    private EntityManager em;

    public RecalculoFinDeMesService(INominaService nominaService) {
        this.nominaService = nominaService;
    }

    @Scheduled(cron = "0 50 23 * * *", zone = "${sistema.zona-horaria:America/Lima}")
    public void recalcularMesSiNoSeHizo() {
        recalcularMesSiNoSeHizo(LocalDate.now());
    }

    void recalcularMesSiNoSeHizo(LocalDate hoy) {
        if (hoy.plusDays(1).getMonthValue() == hoy.getMonthValue()) {
            return;
        }
        YearMonth mes = YearMonth.from(hoy);
        if (huboCalculoEn(mes)) {
            return;
        }
        try {
            nominaService.calcularNomina(
                    new CalcularNominaRequestDTO(mes.atDay(1), mes.atEndOfMonth(), null, null),
                    ID_USUARIO_SISTEMA);
            log.info("Nómina del mes {} recalculada automáticamente: no hubo cálculos durante el mes", mes);
        } catch (RuntimeException e) {
            log.warn("No se pudo recalcular automáticamente la nómina de {}: {}", mes, e.getMessage());
        }
    }

    private boolean huboCalculoEn(YearMonth mes) {
        Number cantidad = (Number) em.createNativeQuery(
                "SELECT count(*) FROM historial_auditoria WHERE accion = 'CALCULO_NOMINA' "
                + "AND fecha_de_cambio >= :desde AND fecha_de_cambio < :hasta")
            .setParameter("desde", mes.atDay(1).atStartOfDay())
            .setParameter("hasta", mes.plusMonths(1).atDay(1).atStartOfDay())
            .getSingleResult();
        return cantidad.longValue() > 0;
    }
}
