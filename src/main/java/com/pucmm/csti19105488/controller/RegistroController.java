package com.pucmm.csti19105488.controller;

import com.pucmm.csti19105488.dao.CodigoQRDAO;
import com.pucmm.csti19105488.model.CodigoQR;
import com.pucmm.csti19105488.model.Evento;
import com.pucmm.csti19105488.model.Registro;
import com.pucmm.csti19105488.model.Usuario;
import com.pucmm.csti19105488.service.EstadisticaService;
import com.pucmm.csti19105488.service.EventoService;
import com.pucmm.csti19105488.service.RegistroService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.javalin.apibuilder.ApiBuilder.*;

public class RegistroController {

    private final RegistroService registroService = new RegistroService();
    private final EstadisticaService estadisticasService = new EstadisticaService();
    private final CodigoQRDAO codigoQRDAO = new CodigoQRDAO();

    public void registrarRutas() {

        // Inscribirse a evento (Fetch API)
        post("/api/eventos/{id}/inscribir", ctx -> {
            Usuario u = ctx.sessionAttribute("usuario");
            if (!UsuarioController.estaAutenticado(ctx) && !u.getRol().equals("PARTICIPANTE")) { ctx.status(401).result("No autenticado"); return; }

            String error = registroService.inscribir(u, Long.parseLong(ctx.pathParam("id")));
            if (error != null) {
                ctx.status(400).json(Map.of("error", error));
            } else {
                ctx.json(Map.of("mensaje", "Inscripción exitosa"));
            }
        });

        // Cancelar inscripción (Fetch API)
        post("/api/eventos/{id}/cancelar-inscripcion", ctx -> {
            Usuario u = ctx.sessionAttribute("usuario");
            if (u == null) { ctx.status(401).json(Map.of("error", "No autenticado")); return; }
            Long eventoId = Long.parseLong(ctx.pathParam("eventoId"));
            Evento evento = EventoService.buscarPorId(eventoId);

            if (evento == null) {
                ctx.status(404).json(Map.of("error", "Evento no encontrado"));
                return;
            }
            if (evento.getFecha().isBefore(LocalDateTime.now())) {
                ctx.status(400).json(Map.of("error", "No puedes cancelar una inscripción de un evento que ya ocurrió"));
                return;
            }

            try {
                registroService.cancelarInscripcion(u, eventoId);
                ctx.json(Map.of("mensaje", "Inscripción cancelada correctamente"));
            } catch (Exception e) {
                ctx.status(400).json(Map.of("error", e.getMessage()));
            }
        });

        // Validar QR y marcar asistencia (Fetch API)
        post("/api/asistencia/validar", ctx -> {

            if (!UsuarioController.esOrganizador(ctx) && !UsuarioController.esAdmin(ctx)) {
                ctx.status(403).result("Sin permisos");
                return;
            }

            Usuario usuario = ctx.sessionAttribute("usuario");

            Map<String, String> body = ctx.bodyAsClass(Map.class);
            String token = body.get("token");

            String error = registroService.marcarAsistencia(token, usuario);

            if (error != null) {
                ctx.status(400).json(Map.of("error", error));
            } else {
                ctx.json(Map.of("mensaje", "Asistencia registrada correctamente"));
            }
        });

        // Mis inscripciones (participante)
        get("/mis-inscripciones", ctx -> {
            if (!UsuarioController.estaAutenticado(ctx)) { ctx.redirect("/login"); return; }
            Usuario u = ctx.sessionAttribute("usuario");
            List<Registro> registros = registroService.listarPorUsuario(u.getId());

            // Obtener el QR de cada registro
            Map<String, String> qrImages = new HashMap<>();
            for (Registro r : registros) {
                CodigoQR qr = codigoQRDAO.buscarPorRegistro(r.getId());
                System.out.println("Registro ID: " + r.getId() + " -> QR: " + (qr != null ? qr.getId() : "NULL"));
                if (qr != null) {
                    qrImages.put(String.valueOf(r.getId()), qr.getImageBase64());
                }
            }

            Map<String, Object> model = new HashMap<>();
            model.put("usuario", u);
            model.put("registros", registros);
            model.put("qrImages", qrImages);
            ctx.render("/participante/mis-inscripciones.html", model);
        });

        // Resumen del evento con estadísticas (Fetch API)
        get("/api/eventos/{id}/resumen", ctx -> {
            if (!UsuarioController.esOrganizador(ctx) && !UsuarioController.esAdmin(ctx)) { ctx.status(403).result("Sin permisos"); return; }
            Long eventoId = Long.parseLong(ctx.pathParam("id"));
            ctx.json(estadisticasService.generarResumen(eventoId));
        });

        // Página de resumen del evento
        get("/organizador/eventos/{id}/resumen", ctx -> {
            if (!UsuarioController.esOrganizador(ctx) && !UsuarioController.esAdmin(ctx)) { ctx.redirect("/login"); return; }
            Map<String, Object> model = new HashMap<>();
            model.put("usuario", ctx.sessionAttribute("usuario"));
            model.put("eventoId", ctx.pathParam("id"));
            ctx.render("/organizador/resumen.html", model);
        });

        // Página del escáner QR
        get("/organizador/eventos/{id}/scanner", ctx -> {
            System.out.println("Intentando cargar scanner para ID: " + ctx.pathParam("id"));
            if (!UsuarioController.esOrganizador(ctx) && !UsuarioController.esAdmin(ctx)) {
                System.out.println("Redirigiendo a login: No es organizador");
                ctx.redirect("/login");
                return;
            }
            System.out.println("Es organizador, preparando model");
            Map<String, Object> model = new HashMap<>();
            model.put("usuario", ctx.sessionAttribute("usuario"));
            model.put("eventoId", ctx.pathParam("id"));
            System.out.println("Renderizando scanner.html");
            ctx.render("/organizador/scanner.html", model);
        });
    }
}