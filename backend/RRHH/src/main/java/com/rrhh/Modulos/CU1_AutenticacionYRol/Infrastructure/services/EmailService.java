package com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import jakarta.mail.internet.MimeMessage;

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

    public void enviarComprobanteNomina(
            String destinatario,
            String nombreEmpleado,
            String periodo,
            byte[] pdf
    ) throws Exception {

        MimeMessage mensaje = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mensaje, true);

        helper.setTo(destinatario);
        helper.setSubject("Comprobante de Nomina - " + periodo + " - Hospital San Gabriel");

        helper.setText(
                "Estimado/a " + nombreEmpleado + ",\n\n"
                + "Adjunto encontrara su comprobante de nomina correspondiente al periodo " + periodo + ".\n\n"
                + "Saludos,\n"
                + "Departamento de RRHH - Hospital San Gabriel"
        );

        helper.addAttachment(
                "Comprobante_Nomina_" + periodo + "_" + nombreEmpleado.replace(" ", "_") + ".pdf",
                new ByteArrayResource(pdf)
        );

        mailSender.send(mensaje);
    }
}