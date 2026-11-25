package com.rrhh.Modulos.CU3_Nomina.Presentation.controllers;

import com.rrhh.Modulos.CU3_Nomina.Application.services.LiquidacionHorasExtraService;
import com.rrhh.Shared.config.RRHHProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("HorasExtraDetalleController - umbral de horas dobles (RNF46)")
class HorasExtraDetalleControllerTest {

    @Mock private LiquidacionHorasExtraService liquidacion;
    @Mock private RRHHProperties rrhhProperties;

    @Test
    @DisplayName("El endpoint de umbral devuelve el valor configurado en rrhh.umbral-horas-dobles")
    void umbral_DevuelveElValorConfigurado() {
        when(rrhhProperties.getUmbralHorasDobles()).thenReturn(3);
        HorasExtraDetalleController controller = new HorasExtraDetalleController(liquidacion, rrhhProperties);

        assertEquals(3, controller.umbral().getBody().get("umbralHorasDobles"));
    }
}
