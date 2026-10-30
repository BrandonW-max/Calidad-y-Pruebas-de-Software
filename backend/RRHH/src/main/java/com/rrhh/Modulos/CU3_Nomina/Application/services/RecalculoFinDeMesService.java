package com.rrhh.Modulos.CU3_Nomina.Application.services;

import com.rrhh.Modulos.CU3_Nomina.Application.dto.CalcularNominaRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;

@Service
public class RecalculoFinDeMesService {

    private static final Logger log = LoggerFactory.getLogger(RecalculoFinDeMesService.class);
    @org.springframework.beans.factory.annotation.Value("${sistema.usuario-automatico:1}")
    private Long idUsuarioSistema = 1L;

    private final INominaService nominaService;

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
        // Siempre al cierre del mes: el cálculo solo liquida los horarios nuevos, así que los últimos días no se pierden
        YearMonth mes = YearMonth.from(hoy);
        try {
            nominaService.calcularNomina(
                    new CalcularNominaRequestDTO(mes.atDay(1), mes.atEndOfMonth(), null, null),
                    idUsuarioSistema);
            log.info("Nómina del mes {} recalculada automáticamente al cierre", mes);
        } catch (RuntimeException e) {
            log.warn("No se pudo recalcular automáticamente la nómina de {}: {}", mes, e.getMessage());
        }
    }
}
