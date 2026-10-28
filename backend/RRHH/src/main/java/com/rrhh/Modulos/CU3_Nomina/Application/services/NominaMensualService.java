package com.rrhh.Modulos.CU3_Nomina.Application.services;

import com.rrhh.Modulos.CU3_Nomina.Application.dto.CalcularNominaRequestDTO;
import com.rrhh.Modulos.CU3_Nomina.Domain.repository.INominaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.YearMonth;

@Service
public class NominaMensualService {

    private static final Logger log = LoggerFactory.getLogger(NominaMensualService.class);
    private static final Long ID_USUARIO_SISTEMA = 1L;

    private final INominaService nominaService;
    private final INominaRepository nominaRepository;
    private final int mesesAtras;

    public NominaMensualService(INominaService nominaService,
                                INominaRepository nominaRepository,
                                @Value("${nomina.mensual.meses-atras:12}") int mesesAtras) {
        this.nominaService = nominaService;
        this.nominaRepository = nominaRepository;
        this.mesesAtras = mesesAtras;
    }

    @Scheduled(cron = "0 0 1 1 * *", zone = "${sistema.zona-horaria:America/Lima}")
    public void calcularPeriodosPendientes() {
        YearMonth actual = YearMonth.now();
        for (int i = 1; i <= mesesAtras; i++) {
            YearMonth periodo = actual.minusMonths(i);
            if (!nominaRepository.buscarPorPeriodo(periodo.toString()).isEmpty()) {
                continue;
            }
            CalcularNominaRequestDTO request = new CalcularNominaRequestDTO(
                    periodo.atDay(1), periodo.atEndOfMonth(), null, null);
            try {
                nominaService.calcularNomina(request, ID_USUARIO_SISTEMA);
                log.info("Nómina generada automáticamente para el período {}", periodo);
            } catch (RuntimeException e) {
                log.warn("No se pudo generar la nómina del período {}: {}", periodo, e.getMessage());
            }
        }
    }
}
