package com.pucmm.csti19105488.service;

import com.pucmm.csti19105488.dao.CodigoQRDAO;
import com.pucmm.csti19105488.dao.EventoDAO;
import com.pucmm.csti19105488.dao.RegistroDAO;
import com.pucmm.csti19105488.model.*;
import com.google.zxing.*;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public class RegistroService {

    private static final RegistroDAO registroDAO = new RegistroDAO();
    private final EventoDAO eventoDAO = new EventoDAO();
    private final CodigoQRDAO codigoQRDAO = new CodigoQRDAO();

    public String inscribir(Usuario participante, Long eventoId) {
        Evento evento = eventoDAO.buscarPorId(eventoId);
        if (evento == null) return "Evento no encontrado";
        if (!evento.tieneDisponibilidad()) return "El evento no tiene cupo disponible";
        if (registroDAO.existeInscripcion(participante.getId(), eventoId)) return "Ya estás inscrito en este evento";

        // Generar token único
        String token = UUID.randomUUID().toString();

        // Crear y guardar registro
        Registro registro = new Registro(participante, evento, token);
        registroDAO.guardar(registro);

        // Recargar el registro para obtener el ID generado
        Registro registroGuardado = registroDAO.buscarPorToken(token);
        System.out.println("Registro guardado con ID: " + registroGuardado.getId());

        // Actualizar inscritos
        evento.setInscritosActuales(evento.getInscritosActuales() + 1);
        eventoDAO.actualizar(evento);

        // Generar QR
        try {
            String qrContent = "eventoId=" + eventoId + "&usuarioId=" + participante.getId() + "&token=" + token;
            String imageBase64 = generarQRBase64(qrContent);
            CodigoQR codigoQR = new CodigoQR(registroGuardado, token, imageBase64);
            codigoQRDAO.guardar(codigoQR);
            System.out.println("QR guardado correctamente para registro ID: " + registroGuardado.getId());
        } catch (Exception e) {
            System.err.println("Error generando QR: " + e.getMessage());
        }
        return null;
    }

    public String cancelarInscripcion(Usuario participante, Long eventoId) {
        Evento evento = eventoDAO.buscarPorId(eventoId);
        if (evento == null) return "Evento no encontrado";
        if (evento.getFechaFin().isBefore(LocalDateTime.now())) return "No puedes cancelar después de la fecha del evento";

        List<Registro> registros = registroDAO.listarPorUsuario(participante.getId());
        Registro registro = registros.stream()
                .filter(r -> r.getEvento().getId().equals(eventoId))
                .findFirst().orElse(null);

        if (registro == null) return "No estás inscrito en este evento";

        codigoQRDAO.eliminarPorRegistro(registro.getId());
        registroDAO.eliminar(registro.getId());
        evento.setInscritosActuales(evento.getInscritosActuales() - 1);
        eventoDAO.actualizar(evento);
        return null;
    }

    public String marcarAsistencia(String token, Usuario organizador) {

        Registro registro = registroDAO.buscarPorToken(token);
        if (registro == null) return "QR inválido";

        LocalDate hoy = LocalDate.now();

        if (registro.getFechaAsistencia() != null &&
                registro.getFechaAsistencia().toLocalDate().equals(hoy)) {
            return "Ya se registró asistencia hoy";
        }

        registro.setAsistenciaConfirmada(true);
        registro.setFechaAsistencia(LocalDateTime.now());
        registro.setQuienConfirmaAsistencia(organizador);

        registroDAO.actualizar(registro);
        return null;
    }

    public static List<Registro> listarPorEvento(Long eventoId) {
        return registroDAO.listarPorEvento(eventoId);
    }

    public static List<Registro> listarPorUsuario(Long usuarioId) {
        return registroDAO.listarPorUsuario(usuarioId);
    }

    private String generarQRBase64(String contenido) throws Exception {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix = writer.encode(contenido, BarcodeFormat.QR_CODE, 300, 300);
        BufferedImage image = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < 300; x++) {
            for (int y = 0; y < 300; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? 0x000000 : 0xFFFFFF);
            }
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

}