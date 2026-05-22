package com.rrhh.AutenticacionYRol.Infrastructure.services;

import java.io.ByteArrayOutputStream;

import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;

@Service
public class TwoFactorService {

    private final GoogleAuthenticator gAuth =
            new GoogleAuthenticator();

    public String generarSecret() {

        GoogleAuthenticatorKey key =
                gAuth.createCredentials();

        return key.getKey();
    }

    public boolean verificarCodigo(
            String secret,
            int codigo
    ) {

        return gAuth.authorize(secret, codigo);
    }

    public String generarQRUrl(
            String username,
            String secret
    ) {

        return "otpauth://totp/RRHH:"
                + username
                + "?secret="
                + secret
                + "&issuer=RRHH_SYSTEM";
    }

    // GENERAR IMAGEN QR
    public byte[] generarQRImagen(
            String qrText
    ) throws Exception {

        QRCodeWriter qrCodeWriter =
                new QRCodeWriter();

        var bitMatrix =
                qrCodeWriter.encode(
                        qrText,
                        BarcodeFormat.QR_CODE,
                        250,
                        250
                );

        ByteArrayOutputStream pngOutputStream =
                new ByteArrayOutputStream();

        MatrixToImageWriter.writeToStream(
                bitMatrix,
                "PNG",
                pngOutputStream
        );

        return pngOutputStream.toByteArray();
    }
}