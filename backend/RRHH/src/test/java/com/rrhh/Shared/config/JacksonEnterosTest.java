package com.rrhh.Shared.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.rrhh.Modulos.CU3_Nomina.Application.dto.AjustarNominaRequestDTO;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.dto.MarcarAsistenciaManualRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JsonTest
class JacksonEnterosTest {

    @Autowired
    private ObjectMapper mapper;

    @Test
    void idEnteroSeDeserializaCorrectamente() throws Exception {
        MarcarAsistenciaManualRequestDTO dto =
                mapper.readValue("{\"idEmpleado\": 7}", MarcarAsistenciaManualRequestDTO.class);

        assertEquals(7, dto.getIdEmpleado());
    }

    @Test
    void idDecimalEnCampoEnteroSeRechaza() {
        assertThrows(InvalidFormatException.class, () ->
                mapper.readValue("{\"idEmpleado\": 1.5}", MarcarAsistenciaManualRequestDTO.class));
    }

    @Test
    void cantidadDecimalEnCampoEnteroSeRechaza() {
        assertThrows(InvalidFormatException.class, () ->
                mapper.readValue("{\"cantidadHijos\": 2.5}", AjustarNominaRequestDTO.class));
    }
}
