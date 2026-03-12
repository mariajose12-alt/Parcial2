package com.pucmm.csti19105488.controller;

import com.pucmm.csti19105488.dao.RegistroDAO;
import com.pucmm.csti19105488.model.Evento;
import com.pucmm.csti19105488.model.Registro;
import com.pucmm.csti19105488.model.Usuario;
import com.pucmm.csti19105488.model.enums.TipoEvento;
import com.pucmm.csti19105488.model.enums.TipoUsuario;
import com.pucmm.csti19105488.service.EstadisticaService;
import com.pucmm.csti19105488.service.EventoService;
import com.pucmm.csti19105488.service.RegistroService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.javalin.apibuilder.ApiBuilder.*;

public class EventoController {

    private final EventoService eventoService = new EventoService();
    private final RegistroService registroService = new RegistroService();
    private final EstadisticaService estadisticasService = new EstadisticaService();

    public void registrarRutas() {

        // Lista de eventos públicos (participantes)
        get("/eventos", ctx -> {
            if (!UsuarioController.estaAutenticado(ctx)) { ctx.redirect("/login"); return; }

            Usuario usuario = ctx.sessionAttribute("usuario");
            List<Evento> eventos = eventoService.listarPublicados();

            for (Evento e : eventos) {
                boolean inscrito = RegistroDAO.existeInscripcion(usuario.getId(), e.getId());
                e.setUsuarioInscrito(inscrito);
            }

            Map<String, Object> model = new HashMap<>();
            model.put("usuario", usuario);
            model.put("eventos", eventos);

            ctx.render("/eventos/lista.html", model);
        });

        // Lista de eventos para el admin (todos) y organizador (owned)
        get("/organizador/eventos", ctx -> {

            if (!UsuarioController.esOrganizador(ctx) && !UsuarioController.esAdmin(ctx)) {
                ctx.redirect("/login");
                return;
            }

            Usuario u = ctx.sessionAttribute("usuario");
            Map<String, Object> model = new HashMap<>();
            model.put("usuario", u);

            if (u.getRol() == TipoUsuario.ADMINISTRADOR) {
                model.put("eventos", eventoService.listarTodos());
            } else {
                model.put("eventos", eventoService.listarPorOrganizador(u.getId()));
            }

            ctx.render("/organizador/eventos.html", model);
        });

        // Formulario crear evento
        get("/organizador/eventos/nuevo", ctx -> {
            if (!UsuarioController.esOrganizador(ctx)) { ctx.redirect("/login"); return; }
            Map<String, Object> model = new HashMap<>();
            model.put("usuario", ctx.sessionAttribute("usuario"));
            model.put("evento", new Evento());
            ctx.render("/organizador/evento-form.html", model);
        });

        // Procesar crear evento
        post("/organizador/eventos/nuevo", ctx -> {

            if (!UsuarioController.esOrganizador(ctx)) {
                ctx.redirect("/login");
                return;
            }

            Usuario u = ctx.sessionAttribute("usuario");
            LocalDateTime inicio = LocalDateTime.parse(ctx.formParam("fechaInicio"));
            LocalDateTime fin = LocalDateTime.parse(ctx.formParam("fechaFin"));

            TipoEvento tipo = TipoEvento.valueOf(ctx.formParam("tipo"));

            try {

                eventoService.crearEvento(
                        ctx.formParam("titulo"),
                        ctx.formParam("descripcion"),
                        inicio,
                        fin,
                        tipo,
                        ctx.formParam("lugar"),
                        Integer.parseInt(ctx.formParam("capacidadMax")),
                        u
                );

                ctx.redirect("/organizador/eventos");

            } catch (RuntimeException e) {

                Map<String, Object> model = new HashMap<>();
                model.put("usuario", u);
                model.put("error", e.getMessage());

                // importante: reenviar datos al form
                Evento evento = new Evento();
                evento.setTitulo(ctx.formParam("titulo"));
                evento.setDescripcion(ctx.formParam("descripcion"));
                evento.setLugar(ctx.formParam("lugar"));
                evento.setCapacidadMax(Integer.parseInt(ctx.formParam("capacidadMax")));

                model.put("evento", evento);

                ctx.render("/organizador/evento-form.html", model);
            }
        });

        // Formulario editar evento
        get("/organizador/eventos/{id}/editar", ctx -> {

            if (!UsuarioController.esOrganizador(ctx) && !UsuarioController.esAdmin(ctx)) {
                ctx.redirect("/login");
                return;
            }

            Long id = Long.parseLong(ctx.pathParam("id"));
            Usuario u = ctx.sessionAttribute("usuario");

            Evento evento = eventoService.buscarPorId(id);

            if (evento == null) {
                ctx.status(404);
                return;
            }

            // Seguridad: organizador solo puede editar los suyos
            if (u.getRol() == TipoUsuario.ORGANIZADOR &&
                    !evento.getOrganizador().getId().equals(u.getId())) {
                ctx.status(403);
                return;
            }

            Map<String, Object> model = new HashMap<>();
            model.put("usuario", u);
            model.put("evento", evento);

            ctx.render("/organizador/evento-form.html", model);
        });

        // Ver/Administrar solo el evento seleccionado (nueva vista)
        get("/organizador/eventos/{id}", ctx -> {
            if (!UsuarioController.esOrganizador(ctx) && !UsuarioController.esAdmin(ctx)) {
                ctx.redirect("/login");
                return;
            }

            Long id = Long.parseLong(ctx.pathParam("id"));
            Usuario u = ctx.sessionAttribute("usuario");
            Evento evento = eventoService.buscarPorId(id);

            if (evento == null) { ctx.status(404); return; }

            if (u.getRol() == TipoUsuario.ORGANIZADOR &&
                    !evento.getOrganizador().getId().equals(u.getId())) {
                ctx.status(403);
                return;
            }

            Map<String, Object> model = new HashMap<>();
            model.put("usuario", u);
            model.put("evento", evento);
            model.put("inscritos", RegistroService.listarPorEvento(evento.getId()));

            ctx.render("/organizador/evento-detalle.html", model);
        });

        // Procesar editar evento
        post("/organizador/eventos/{id}/editar", ctx -> {
            if (!UsuarioController.esOrganizador(ctx) && !UsuarioController.esAdmin(ctx)) { ctx.redirect("/login"); return; }
            Long id = ctx.formParam("id") != null
                    ? Long.parseLong(ctx.formParam("id"))
                    : null;

            TipoEvento tipo = TipoEvento.valueOf(ctx.formParam("tipo"));

            LocalDateTime inicio = LocalDateTime.parse(ctx.formParam("fechaInicio"));
            LocalDateTime fin = LocalDateTime.parse(ctx.formParam("fechaFin"));

            String error = eventoService.editarEvento(
                    id,
                    ctx.formParam("titulo"),
                    ctx.formParam("descripcion"),
                    inicio,
                    fin,
                    tipo,
                    ctx.formParam("lugar"),
                    Integer.parseInt(ctx.formParam("capacidadMax"))
            );
            if (error != null) {
                Map<String, Object> model = new HashMap<>();
                model.put("usuario", ctx.sessionAttribute("usuario"));
                model.put("error", error);
                model.put("evento", eventoService.buscarPorId(id));
                ctx.render("/organizador/evento-form.html", model);
                return;
            }
            ctx.redirect("/organizador/eventos");
        });

        // Cancelar evento
        post("/organizador/eventos/{id}/cancelar", ctx -> {

            if (!UsuarioController.esOrganizador(ctx) && !UsuarioController.esAdmin(ctx)) {
                ctx.redirect("/login");
                return;
            }

            Long id = Long.parseLong(ctx.pathParam("id"));
            Evento evento = eventoService.buscarPorId(id);

            if (evento == null) {
                ctx.status(404).result("Evento no encontrado");
                return;
            }

            // Validación de estado
            if (evento.estaEnCurso()) {
                ctx.status(400).result("No se puede cancelar un evento que ya comenzó");
                return;
            }

            if (evento.haTerminado()) {
                ctx.status(400).result("No se puede cancelar un evento finalizado");
                return;
            }

            eventoService.cancelarEvento(id);

            ctx.redirect("/organizador/eventos");
        });

        // Publicar evento
        post("/organizador/eventos/{id}/publicar", ctx -> {
            if (!UsuarioController.esOrganizador(ctx) && !UsuarioController.esAdmin(ctx)) { ctx.redirect("/login"); return; }
            eventoService.publicarEvento(Long.parseLong(ctx.pathParam("id")));
            ctx.redirect("/organizador/eventos");
        });

        // Despublicar evento
        post("/organizador/eventos/{id}/despublicar", ctx -> {
            if (!UsuarioController.esOrganizador(ctx) && !UsuarioController.esAdmin(ctx)) { ctx.redirect("/login"); return; }
            eventoService.despublicarEvento(Long.parseLong(ctx.pathParam("id")));
            ctx.redirect("/organizador/eventos");
        });

        // Eliminar evento (admin)
        post("/admin/eventos/{id}/eliminar", ctx -> {
            if (!UsuarioController.esAdmin(ctx)) { ctx.redirect("/login"); return; }
            eventoService.eliminarEvento(Long.parseLong(ctx.pathParam("id")));
            ctx.redirect("/admin/eventos");
        });

        // Panel admin - listar todos los eventos
        get("/admin/eventos", ctx -> {
            if (!UsuarioController.esAdmin(ctx)) { ctx.redirect("/login"); return; }
            Map<String, Object> model = new HashMap<>();
            model.put("usuario", ctx.sessionAttribute("usuario"));
            model.put("eventos", eventoService.listarTodos());
            ctx.render("/admin/eventos.html", model);
        });

        // Dashboard compartido
        get("/dashboard", ctx -> {
            if (!UsuarioController.esOrganizador(ctx)) { ctx.redirect("/login"); return; }
            Usuario u = ctx.sessionAttribute("usuario");
            Map<String, Object> model = new HashMap<>();
            model.put("usuario", u);
            if (u.getRol() == TipoUsuario.ADMINISTRADOR) {
                model.put("eventos", eventoService.listarTodos());
            } else {
                model.put("eventos", eventoService.listarPorOrganizador(u.getId()));
            }
            ctx.render("dashboard.html", model);
        });

        // API estadísticas filtradas
        get("/api/dashboard/estadisticas", ctx -> {
            if (!UsuarioController.esOrganizador(ctx)) { ctx.status(403).result("Sin permisos"); return; }
            Usuario u = ctx.sessionAttribute("usuario");
            String eventoId = ctx.queryParam("eventoId");
            String desde = ctx.queryParam("desde");
            String hasta = ctx.queryParam("hasta");
            String tipo = ctx.queryParam("tipo");
            List<Registro> registros = estadisticasService.filtrarRegistros(u, eventoId, desde, hasta, tipo);
            ctx.json(estadisticasService.calcularEstadisticas(registros));
        });
    }
}