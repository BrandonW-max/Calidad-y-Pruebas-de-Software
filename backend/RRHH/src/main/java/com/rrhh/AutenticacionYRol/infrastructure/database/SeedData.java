package com.rrhh.AutenticacionYRol.infrastructure.database;

import com.rrhh.AutenticacionYRol.data.entity.Usuario;
import com.rrhh.AutenticacionYRol.data.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SeedData implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;

    public SeedData(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void run(String... args) {

        if (usuarioRepository.count() == 0) {

            Usuario rrhh = new Usuario();
            rrhh.setUsername("admin_rrhh");
            rrhh.setPassword("1234");
            rrhh.setRol("RRHH");
            rrhh.setActivo(true);

            Usuario gerencia = new Usuario();
            gerencia.setUsername("jefe_gerencia");
            gerencia.setPassword("1234");
            gerencia.setRol("GERENCIA");
            gerencia.setActivo(true);

            Usuario empleado = new Usuario();
            empleado.setUsername("empleado1");
            empleado.setPassword("1234");
            empleado.setRol("EMPLEADO");
            empleado.setActivo(true);

            usuarioRepository.save(rrhh);
            usuarioRepository.save(gerencia);
            usuarioRepository.save(empleado);

            System.out.println("✅ Usuarios seed creados");
        }
    }
}