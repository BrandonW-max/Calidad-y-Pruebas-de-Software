package com.rrhh.AutenticacionYRol.Domain.entities;

import java.time.LocalDateTime;

public class Historial {
    private Long idAuditoria;
    private Long idUsuario;
    private String accion;
    private String tablaAfectada;
    private String ipMaquina;
    private LocalDateTime fechaCambio;
    private String valorAnterior;
    private String valorNuevo;

    
    public Historial() {}

    
    public Historial(Long idUsuario, String accion, String ip) {
        this.idUsuario = idUsuario;
        this.accion = accion;
        this.ipMaquina = ip;
        this.tablaAfectada = "USUARIO";
        this.fechaCambio = LocalDateTime.now();
    }

    
    public Long getIdAuditoria() { return idAuditoria; }
    public void setIdAuditoria(Long idAuditoria) { this.idAuditoria = idAuditoria; }

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }

    public String getTablaAfectada() { return tablaAfectada; }
    public void setTablaAfectada(String tablaAfectada) { this.tablaAfectada = tablaAfectada; }

    public String getIpMaquina() { return ipMaquina; }
    public void setIpMaquina(String ipMaquina) { this.ipMaquina = ipMaquina; }

    public LocalDateTime getFechaCambio() { return fechaCambio; }
    public void setFechaCambio(LocalDateTime fechaCambio) { this.fechaCambio = fechaCambio; }

    public String getValorAnterior() { return valorAnterior; }
    public void setValorAnterior(String valorAnterior) { this.valorAnterior = valorAnterior; }

    public String getValorNuevo() { return valorNuevo; }
    public void setValorNuevo(String valorNuevo) { this.valorNuevo = valorNuevo; }
}
