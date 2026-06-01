package com.rrhh.AutenticacionYRol.infrastructure.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void enviarCorreoRecuperacion(
            String destinatario,
            String linkRecuperacion
    ) {

        SimpleMailMessage mensaje = new SimpleMailMessage();

        mensaje.setTo(destinatario);
        mensaje.setSubject("Recuperación de contraseña - Hospital San Gabriel");

        mensaje.setText(
                "Hola,\n\n"
                + "Se solicitó recuperar la contraseña de tu cuenta.\n\n"
                + "Ingresa al siguiente enlace para crear una nueva contraseña:\n"
                + linkRecuperacion
                + "\n\nEste enlace vencerá en 15 minutos.\n\n"
                + "Si no solicitaste este cambio, ignora este mensaje."
        );

        mailSender.send(mensaje);
    }
}