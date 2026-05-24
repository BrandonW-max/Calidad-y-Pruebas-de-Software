package com.rrhh.GestionEmpleados.Application.services;

import com.rrhh.GestionEmpleados.Application.dto.BuscarEmpleadoResponseDTO;
import com.rrhh.GestionEmpleados.Domain.entities.Contrato;
import com.rrhh.GestionEmpleados.Domain.entities.Empleado;
import com.rrhh.GestionEmpleados.Domain.entities.UsuarioGestion;
import com.rrhh.GestionEmpleados.Domain.repository.IContratoRepository;
import com.rrhh.GestionEmpleados.Domain.repository.IEmpleadoRepository;
import com.rrhh.GestionEmpleados.Domain.repository.IUsuarioGestionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BuscarEmpleadoUseCase {

    @Autowired
    private IEmpleadoRepository empleadoRepository;

    @Autowired
    private IContratoRepository contratoRepository;

    @Autowired
    private IUsuarioGestionRepository usuarioRepository;

    public List<BuscarEmpleadoResponseDTO> listar() {

        List<Empleado> empleados =
                empleadoRepository.findAll();

        return convertirLista(empleados);
    }

    public List<BuscarEmpleadoResponseDTO> buscarPorFiltro(String filtro) {

        List<Empleado> empleados =
                empleadoRepository.buscarPorFiltro(filtro);

        return convertirLista(empleados);
    }

    public List<BuscarEmpleadoResponseDTO> buscarAvanzado(
            String estado,
            Integer idDpto,
            String cargo
    ) {

        String estadoFiltro = null;
        String cargoFiltro = null;

        if (estado != null && !estado.isBlank()) {
            estadoFiltro = estado.toUpperCase();
        }

        if (cargo != null && !cargo.isBlank()) {
            cargoFiltro = cargo;
        }

        List<Empleado> empleados =
                empleadoRepository.buscarAvanzado(
                        estadoFiltro,
                        idDpto,
                        cargoFiltro
                );

        return convertirLista(empleados);
    }

    private List<BuscarEmpleadoResponseDTO> convertirLista(
            List<Empleado> empleados
    ) {

        List<BuscarEmpleadoResponseDTO> respuesta =
                new ArrayList<>();

        for (Empleado empleado : empleados) {
            respuesta.add(convertir(empleado));
        }

        return respuesta;
    }

    private BuscarEmpleadoResponseDTO convertir(
            Empleado empleado
    ) {

        Contrato contrato =
                contratoRepository.findContratoActivoByEmpleado(
                        empleado.getIdEmpleado()
                );

        UsuarioGestion usuario =
                usuarioRepository.findByEmpleadoId(
                        empleado.getIdEmpleado()
                );

        return new BuscarEmpleadoResponseDTO(
                empleado.getIdEmpleado(),
                empleado.getNombres(),
                empleado.getApellidos(),
                empleado.getNumeroDi(),
                empleado.getCorreo(),
                empleado.getTelefono(),
                empleado.getEstado(),
                contrato != null ? contrato.getCargo() : null,
                contrato != null ? contrato.getNombreDepartamento() : null,
                usuario != null ? usuario.getNombreRol() : null,
                usuario != null ? usuario.getNombreUsuario() : null,
                usuario != null ? usuario.getCorreoInst() : null
        );
    }
}