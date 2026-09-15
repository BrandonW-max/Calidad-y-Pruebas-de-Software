package com.rrhh.Modulos.CU3_Nomina.Presentation.controllers;

import com.rrhh.Modulos.CU3_Nomina.Application.dto.CalcularNominaRequestDTO;
import com.rrhh.Modulos.CU3_Nomina.Application.dto.AjustarNominaRequestDTO;
import com.rrhh.Modulos.CU3_Nomina.Application.dto.NominaResponseDTO;
import com.rrhh.Modulos.CU3_Nomina.Application.services.INominaService;
import com.rrhh.Modulos.CU3_Nomina.Presentation.routes.NominaApiRoutes;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.services.EmailService;
import com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces.JpaEmpleadoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.UsuarioGestion;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IUsuarioGestionRepository;
import com.rrhh.Shared.persistence.NotificacionEnvioModel;
import com.rrhh.Shared.security.AuthUtils;
import com.rrhh.config.security.AuthenticatedUser;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
public class NominaController {

    private final INominaService nominaService;
    private final EmailService emailService;
    private final IUsuarioGestionRepository usuarioGestionRepository;
    private final JpaEmpleadoRepository jpaEmpleadoRepository;

    @PersistenceContext
    private EntityManager em;

    public NominaController(INominaService nominaService,
                            EmailService emailService,
                            IUsuarioGestionRepository usuarioGestionRepository,
                            JpaEmpleadoRepository jpaEmpleadoRepository) {
        this.nominaService = nominaService;
        this.emailService = emailService;
        this.usuarioGestionRepository = usuarioGestionRepository;
        this.jpaEmpleadoRepository = jpaEmpleadoRepository;
    }

    @PostMapping(NominaApiRoutes.CALCULAR)
    @PreAuthorize("hasRole('RRHH')")
    public ResponseEntity<List<NominaResponseDTO>> calcularNomina(
            @Valid @RequestBody CalcularNominaRequestDTO request,
            Authentication authentication) {
        Long idUsuario = AuthUtils.extraerIdUsuario(authentication);
        List<NominaResponseDTO> resultado = nominaService.calcularNomina(request, idUsuario);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping(NominaApiRoutes.BUSCAR_ID)
    @PreAuthorize("hasAnyRole('RRHH', 'EMPLEADO')")
    public ResponseEntity<NominaResponseDTO> buscarPorId(
            @PathVariable Integer id,
            @AuthenticationPrincipal AuthenticatedUser user) {
        NominaResponseDTO nomina = nominaService.buscarNominaPorId(id);
        if (hayRol(user, "EMPLEADO") && !esPropia(nomina.getIdEmpleado(), user.getIdUsuario())) {
            throw new AccessDeniedException("No tiene permiso para ver esta nomina.");
        }
        return ResponseEntity.ok(nomina);
    }

    @GetMapping(NominaApiRoutes.POR_PERIODO)
    @PreAuthorize("hasRole('RRHH')")
    public ResponseEntity<List<NominaResponseDTO>> buscarPorPeriodo(
            @PathVariable String periodo) {
        return ResponseEntity.ok(nominaService.buscarPorPeriodo(periodo));
    }

    @GetMapping(NominaApiRoutes.POR_EMPLEADO)
    @PreAuthorize("hasAnyRole('RRHH', 'EMPLEADO')")
    public ResponseEntity<List<NominaResponseDTO>> buscarPorEmpleado(
            @PathVariable Long idEmpleado,
            @AuthenticationPrincipal AuthenticatedUser user) {
        if (hayRol(user, "EMPLEADO") && !esPropia(idEmpleado, user.getIdUsuario())) {
            throw new AccessDeniedException("No tiene permiso para ver nominas de otros empleados.");
        }
        return ResponseEntity.ok(nominaService.buscarPorEmpleado(idEmpleado));
    }

    @GetMapping(NominaApiRoutes.POR_NOMBRE)
    @PreAuthorize("hasRole('RRHH')")
    public ResponseEntity<List<NominaResponseDTO>> buscarPorNombre(@RequestParam String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El parametro 'nombre' es obligatorio.");
        }
        List<NominaResponseDTO> resultados = nominaService.buscarPorNombreEmpleado(nombre);
        return ResponseEntity.ok(resultados);
    }

    @GetMapping(NominaApiRoutes.BUSCAR_ID + "/comprobante")
    @PreAuthorize("hasAnyRole('RRHH', 'EMPLEADO')")
    public ResponseEntity<byte[]> descargarComprobante(
            @PathVariable Integer id,
            @AuthenticationPrincipal AuthenticatedUser user) {
        NominaResponseDTO nomina = nominaService.buscarNominaPorId(id);
        if (hayRol(user, "EMPLEADO") && !esPropia(nomina.getIdEmpleado(), user.getIdUsuario())) {
            throw new AccessDeniedException("No tiene permiso para este comprobante.");
        }
        byte[] pdf = nominaService.generarComprobantePDF(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=Comprobante_Nomina_" + nomina.getPeriodo() + ".pdf")
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_PDF))
                .body(pdf);
    }

    @PutMapping(NominaApiRoutes.AJUSTAR)
    @PreAuthorize("hasRole('RRHH')")
    public ResponseEntity<NominaResponseDTO> ajustarNomina(
            @PathVariable Integer id,
            @Valid @RequestBody AjustarNominaRequestDTO request,
            Authentication authentication) {
        Long idUsuario = AuthUtils.extraerIdUsuario(authentication);
        return ResponseEntity.ok(nominaService.ajustarNomina(id, request, idUsuario));
    }

    @PostMapping(NominaApiRoutes.BUSCAR_ID + "/enviar")
    @PreAuthorize("hasRole('RRHH')")
    public ResponseEntity<Map<String, Object>> enviarComprobanteEmail(@PathVariable Integer id) {
        try {
            NominaResponseDTO nomina = nominaService.buscarNominaPorId(id);
            byte[] pdf = nominaService.generarComprobantePDF(id);

            var empleadoOpt = jpaEmpleadoRepository.findById(Objects.requireNonNull(nomina.getIdEmpleado()));
            if (empleadoOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Empleado no encontrado."));
            }

            var empleado = empleadoOpt.get();
            String correo = empleado.getCorreo() != null ? empleado.getCorreo() : "";

            if (correo.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message",
                        "El empleado no tiene correo registrado."));
            }

            String errorMsg = null;

            try {
                int intentosUsados = emailService.enviarComprobanteNominaConReintentos(correo,
                        empleado.getNombres() + " " + empleado.getApellidos(),
                        nomina.getPeriodo(), pdf, 3);

                NotificacionEnvioModel notif = new NotificacionEnvioModel();
                notif.setIdNomina(id);
                notif.setTipo("COMPROBANTE_NOMINA");
                notif.setEstado("ENVIADO");
                notif.setIntentos(intentosUsados);
                notif.setFechaEnvio(java.time.LocalDateTime.now());
                em.persist(notif);

                return ResponseEntity.ok(Map.of("success", true, "message",
                        "Comprobante enviado a " + correo));
            } catch (Exception e) {
                errorMsg = e.getMessage();
            }

            NotificacionEnvioModel notif = new NotificacionEnvioModel();
            notif.setIdNomina(id);
            notif.setTipo("COMPROBANTE_NOMINA");
            notif.setEstado("FALLIDO");
            notif.setIntentos(3);
            notif.setError(errorMsg);
            em.persist(notif);

            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "Error al enviar tras 3 intentos: " + errorMsg));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "Error al enviar: " + e.getMessage()));
        }
    }

    private boolean hayRol(AuthenticatedUser user, String rol) {
        return user != null && user.getRol() != null && user.getRol().equalsIgnoreCase(rol);
    }

    private boolean esPropia(Long idEmpleadoNomina, Long idUsuario) {
        if (idUsuario == null || idEmpleadoNomina == null) return false;
        UsuarioGestion usuario = usuarioGestionRepository.findByEmpleadoId(idEmpleadoNomina);
        return usuario != null && usuario.getIdUsuario() != null
                && usuario.getIdUsuario().equals(idUsuario);
    }
}