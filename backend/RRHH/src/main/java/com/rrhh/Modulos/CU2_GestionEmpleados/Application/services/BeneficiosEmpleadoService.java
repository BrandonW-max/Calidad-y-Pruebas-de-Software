package com.rrhh.Modulos.CU2_GestionEmpleados.Application.services;

import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.FamiliarRequestDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.PerfilNominaRequestDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces.JpaEmpleadoDerechoHabientesRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces.JpaEmpleadoRepository;
import com.rrhh.Modulos.CU3_Nomina.Infrastructure.interfaces.JpaFamiliaInfoRepository;
import com.rrhh.Shared.exception.RecursoNoEncontradoException;
import com.rrhh.Shared.persistence.EmpleadoDerechoHabientesModel;
import com.rrhh.Shared.persistence.EmpleadoModel;
import com.rrhh.Shared.persistence.FamiliaInfoModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

@Service
public class BeneficiosEmpleadoService {

    private final JpaEmpleadoRepository empleados;
    private final JpaEmpleadoDerechoHabientesRepository perfiles;
    private final JpaFamiliaInfoRepository familiares;

    public BeneficiosEmpleadoService(JpaEmpleadoRepository empleados, JpaEmpleadoDerechoHabientesRepository perfiles,
                                      JpaFamiliaInfoRepository familiares) {
        this.empleados = empleados;
        this.perfiles = perfiles;
        this.familiares = familiares;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obtener(Long idEmpleado) {
        EmpleadoDerechoHabientesModel perfil = perfiles.findByEmpleadoIdEmpleado(idEmpleado).orElse(null);
        return Map.of("perfil", perfil == null ? Map.of() : perfilMap(perfil),
                "familiares", familiares.findByEmpleadoIdEmpleadoAndActivoTrue(idEmpleado).stream().map(this::familiarMap).toList());
    }

    @Transactional
    public Map<String, Object> guardarPerfil(Long idEmpleado, PerfilNominaRequestDTO dto) {
        EmpleadoModel empleado = empleados.findById(Objects.requireNonNull(idEmpleado))
                .orElseThrow(() -> new RecursoNoEncontradoException("Empleado no encontrado"));
        String pension = "AFP".equalsIgnoreCase(dto.regimenPension) ? "AFP" : "ONP";
        EmpleadoDerechoHabientesModel p = perfiles.findByEmpleadoIdEmpleado(idEmpleado).orElseGet(EmpleadoDerechoHabientesModel::new);
        p.setEmpleado(empleado);
        p.setRegimenSalud(dto.regimenSalud == null ? "ESSALUD" : dto.regimenSalud.toUpperCase());
        p.setRegimenPension(pension);
        p.setAfp("AFP".equals(pension) ? dto.afp : null);
        p.setCuspp("AFP".equals(pension) ? dto.cuspp : null);
        p.setTipoComisionAfp("AFP".equals(pension) ? dto.tipoComisionAfp : null);
        p.setContactoEmergencia(dto.contactoEmergencia);
        p.setTelefonoEmergencia(dto.telefonoEmergencia);
        p.setVigenteDesde(dto.vigenteDesde == null ? LocalDate.now() : dto.vigenteDesde);
        p.setVigenteHasta(dto.vigenteHasta);
        p.setActivo(true);
        p.setActualizadoEl(LocalDateTime.now());
        return perfilMap(perfiles.save(p));
    }

    @Transactional
    public Map<String, Object> agregarFamiliar(Long idEmpleado, FamiliarRequestDTO dto) {
        EmpleadoModel empleado = empleados.findById(Objects.requireNonNull(idEmpleado))
                .orElseThrow(() -> new RecursoNoEncontradoException("Empleado no encontrado"));
        if (dto.nombres == null || dto.nombres.isBlank() || dto.fechaNacimiento == null) {
            throw new IllegalArgumentException("Nombres y fecha de nacimiento son requeridos");
        }
        FamiliaInfoModel f = new FamiliaInfoModel();
        aplicarFamiliar(f, dto);
        f.setEmpleado(empleado);
        f.setActivo(true);
        return familiarMap(familiares.save(f));
    }

    @Transactional
    public Map<String, Object> actualizarFamiliar(Long idEmpleado, Integer idFamiliar, FamiliarRequestDTO dto) {
        FamiliaInfoModel f = familiares.findById(Objects.requireNonNull(idFamiliar))
                .filter(x -> x.getEmpleado().getIdEmpleado().equals(idEmpleado))
                .orElseThrow(() -> new RecursoNoEncontradoException("Familiar no encontrado"));
        aplicarFamiliar(f, dto);
        return familiarMap(familiares.save(Objects.requireNonNull(f)));
    }

    @Transactional
    public void desactivarFamiliar(Long idEmpleado, Integer idFamiliar) {
        FamiliaInfoModel f = familiares.findById(Objects.requireNonNull(idFamiliar))
                .filter(x -> x.getEmpleado().getIdEmpleado().equals(idEmpleado))
                .orElseThrow(() -> new RecursoNoEncontradoException("Familiar no encontrado"));
        f.setActivo(false);
        familiares.save(f);
    }

    private void aplicarFamiliar(FamiliaInfoModel f, FamiliarRequestDTO d) {
        f.setNombres(d.nombres);
        f.setApellidos(d.apellidos);
        f.setParentesco(d.parentesco == null ? "HIJO" : d.parentesco.toUpperCase());
        f.setNumeroDi(d.numeroDi);
        f.setFechaNacimiento(d.fechaNacimiento);
        f.setEsDerechohabiente(Boolean.TRUE.equals(d.esDerechohabiente));
        f.setElegibleAsignacionFamiliar(Boolean.TRUE.equals(d.elegibleAsignacionFamiliar));
        f.setValidado(Boolean.TRUE.equals(d.validado));
        f.setCoberturaDesde(d.coberturaDesde);
        f.setCoberturaHasta(d.coberturaHasta);
        f.setActualizadoEl(LocalDateTime.now());
    }

    private Map<String, Object> perfilMap(EmpleadoDerechoHabientesModel p) {
        return Map.of(
                "regimenSalud", n(p.getRegimenSalud()),
                "regimenPension", n(p.getRegimenPension()),
                "afp", n(p.getAfp()),
                "cuspp", n(p.getCuspp()),
                "tipoComisionAfp", n(p.getTipoComisionAfp()),
                "contactoEmergencia", n(p.getContactoEmergencia()),
                "telefonoEmergencia", n(p.getTelefonoEmergencia()),
                "vigenteDesde", nDate(p.getVigenteDesde()),
                "vigenteHasta", nDate(p.getVigenteHasta())
        );
    }

    private Map<String, Object> familiarMap(FamiliaInfoModel f) {
        return Map.ofEntries(
                Map.entry("id", f.getIdFamiliaInfo()),
                Map.entry("nombres", f.getNombres()),
                Map.entry("apellidos", n(f.getApellidos())),
                Map.entry("parentesco", f.getParentesco()),
                Map.entry("numeroDi", n(f.getNumeroDi())),
                Map.entry("fechaNacimiento", nDate(f.getFechaNacimiento())),
                Map.entry("esDerechohabiente", Boolean.TRUE.equals(f.getEsDerechohabiente())),
                Map.entry("elegibleAsignacionFamiliar", Boolean.TRUE.equals(f.getElegibleAsignacionFamiliar())),
                Map.entry("validado", Boolean.TRUE.equals(f.getValidado())),
                Map.entry("coberturaDesde", nDate(f.getCoberturaDesde())),
                Map.entry("coberturaHasta", nDate(f.getCoberturaHasta()))
        );
    }

    private String n(String v) { return v == null ? "" : v; }
    private String nDate(LocalDate v) { return v == null ? "" : v.toString(); }
}
