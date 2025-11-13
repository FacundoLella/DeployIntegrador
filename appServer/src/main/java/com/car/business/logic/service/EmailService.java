package com.car.business.logic.service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.car.business.domain.Alquiler;
import com.car.business.domain.Cliente;
import com.car.business.domain.Contacto;
import com.car.business.domain.ContactoCorreoElectronico;
import com.car.business.domain.Promocion;

@Service
public class EmailService {

    private final JavaMailSender emailSender;
    private static final DateTimeFormatter PROMO_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public EmailService(final JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    public void sendEmail(Alquiler a) {
        for (Contacto c : a.getCliente().getContactos()) {
            if (c instanceof ContactoCorreoElectronico contactoCorreo) {
                try {
                    MimeMessage message = emailSender.createMimeMessage();
                    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                    helper.setFrom("bdfioritech@gmail.com");
                    helper.setTo(contactoCorreo.getEmail());
                    helper.setSubject("Recordatorio: devolución del vehículo mañana");

                    String html = """
                        <html>
                          <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                            <h2 style="color: #2C3E50;">Recordatorio de devolución del vehículo</h2>
                            <p>Hola <b>%s %s (%s)</b>,</p>

                            <p>Te recordamos que la devolución de tu vehículo es el <b>%s</b>.</p>

                            <table style="border-collapse: collapse; margin-top: 10px;">
                              <tr><td><b>Modelo:</b></td><td>%s</td></tr>
                              <tr><td><b>Marca:</b></td><td>%s</td></tr>
                              <tr><td><b>Patente:</b></td><td>%s</td></tr>
                            </table>

                            <p>¡Gracias por confiar en nosotros! 🚗</p>

                            <hr>
                            <small style="color: #888;">FioriTech RentCar © 2025</small>
                          </body>
                        </html>
                        """.formatted(
                            a.getCliente().getNombre(),
                            a.getCliente().getApellido(),
                            a.getCliente().getNumeroDocumento(),
                            a.getFechaHasta(),
                            a.getVehiculo().getCaracteristicaVehiculo().getModelo(),
                            a.getVehiculo().getCaracteristicaVehiculo().getMarca(),
                            a.getVehiculo().getPatente()
                        );

                    helper.setText(html, true); // 👈 true indica que es HTML

                    emailSender.send(message);
                } catch (MessagingException e) {
                    throw new RuntimeException("Error al enviar el correo HTML", e);
                }
            }
        }
    }

    public boolean sendPromociones(Cliente cliente, List<Promocion> promociones) {
        if (cliente == null || CollectionUtils.isEmpty(promociones)) {
            return false;
        }
        List<String> destinatarios = cliente.getContactos().stream()
                .filter(c -> c instanceof ContactoCorreoElectronico)
                .map(c -> ((ContactoCorreoElectronico) c).getEmail())
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
        if (destinatarios.isEmpty()) {
            return false;
        }
        String html = construirHtmlPromociones(cliente, promociones);
        for (String correo : destinatarios) {
            try {
                MimeMessage message = emailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setFrom("bdfioritech@gmail.com");
                helper.setTo(correo);
                helper.setSubject("Nuevas promociones FioriRentCar");
                helper.setText(html, true);
                emailSender.send(message);
            } catch (MessagingException e) {
                throw new RuntimeException("Error al enviar promociones por correo", e);
            }
        }
        return true;
    }

    private String construirHtmlPromociones(Cliente cliente, List<Promocion> promociones) {
        StringBuilder tabla = new StringBuilder();
        tabla.append("""
            <table style="width:100%%; border-collapse: collapse;">
              <thead>
                <tr style="background:#f3f4f6;">
                  <th style="text-align:left; padding:8px;">Código</th>
                  <th style="text-align:left; padding:8px;">Descripción</th>
                  <th style="text-align:left; padding:8px;">Descuento</th>
                  <th style="text-align:left; padding:8px;">Vigencia</th>
                </tr>
              </thead>
              <tbody>
        """);
        promociones.forEach(promo -> {
            tabla.append("<tr>")
                    .append("<td style=\"padding:8px;border-bottom:1px solid #e5e7eb;\">").append(promo.getCodigoDescuento()).append("</td>")
                    .append("<td style=\"padding:8px;border-bottom:1px solid #e5e7eb;\">").append(promo.getDescripcionDescuento()).append("</td>")
                    .append("<td style=\"padding:8px;border-bottom:1px solid #e5e7eb;\">").append(String.format(Locale.US, "%.0f%%", promo.getPorcentajeDescuento())).append("</td>")
                    .append("<td style=\"padding:8px;border-bottom:1px solid #e5e7eb;\">")
                    .append(formatearFechaPromocion(promo.getFechaDesde()))
                    .append(" - ")
                    .append(formatearFechaPromocion(promo.getFechaHasta()))
                    .append("</td>")
                    .append("</tr>");
        });
        tabla.append("""
              </tbody>
            </table>
        """);

        return """
            <html>
              <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #1f2937;">
                <h2 style="color: #111827;">¡Tenemos promociones pensadas para vos, %s!</h2>
                <p>Aprovechá estos beneficios exclusivos disponibles por tiempo limitado:</p>
                %s
                <p style="margin-top:16px;">Reservá tu próximo vehículo con estos códigos y obtené el descuento correspondiente.</p>
                <p>¡Te esperamos!</p>
                <hr>
                <small style="color: #9ca3af;">FioriRentCar · Promociones automáticas</small>
              </body>
            </html>
            """.formatted(cliente.getNombre(), tabla.toString());
    }

    private String formatearFechaPromocion(java.time.LocalDate fecha) {
        return fecha != null ? PROMO_DATE_FORMAT.format(fecha) : "-";
    }
}
