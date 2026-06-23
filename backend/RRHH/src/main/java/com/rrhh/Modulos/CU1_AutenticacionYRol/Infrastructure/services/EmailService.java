package com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.services;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import jakarta.mail.internet.MimeMessage;

import java.util.Objects;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

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

        helper.setTo(Objects.requireNonNull(destinatario));
        helper.setSubject("Comprobante de Nomina - " + periodo + " - Hospital San Gabriel");

        helper.setText(
                "Estimado/a " + nombreEmpleado + ",\n\n"
                + "Adjunto encontrara su comprobante de nomina correspondiente al periodo " + periodo + ".\n\n"
                + "Saludos,\n"
                + "Departamento de RRHH - Hospital San Gabriel"
        );

        helper.addAttachment(
                "Comprobante_Nomina_" + periodo + "_" + nombreEmpleado.replace(" ", "_") + ".pdf",
                new ByteArrayResource(Objects.requireNonNull(pdf))
        );

        mailSender.send(mensaje);
    }

    public void enviarLinkDescargaApp(String destinatario, String nombreEmpleado) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(destinatario);
        mensaje.setSubject("Descarga la App de Asistencia - Hospital San Gabriel");
        mensaje.setText(
            "Estimado/a " + nombreEmpleado + ",\n\n"
            + "Se te ha enviado el enlace para descargar la aplicación móvil del\n"
            + "Hospital San Gabriel para registrar tu asistencia con huella dactilar.\n\n"
            + "Descarga la app aquí:\n"
            + "https://expo.dev/@hospital-san-gabriel/asistencia\n\n"
            + "Pasos:\n"
            + "1. Descarga la app desde el enlace\n"
            + "2. Ingresa tu número de empleado\n"
            + "3. Registra tu huella dactilar\n"
            + "4. ¡Listo! Podrás registrar tu entrada y salida diariamente.\n\n"
            + "Saludos,\n"
            + "Departamento de RRHH - Hospital San Gabriel"
        );
        mailSender.send(mensaje);
    }
}