package com.pucmm.csti19105488.controller;

import com.pucmm.csti19105488.dao.UsuarioDAO;
import com.pucmm.csti19105488.model.Usuario;
import com.pucmm.csti19105488.service.RegistroService;
import com.pucmm.csti19105488.service.UsuarioService;
import com.pucmm.csti19105488.model.enums.TipoUsuario;
import io.javalin.http.Context;
import org.mindrot.jbcrypt.BCrypt;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.javalin.apibuilder.ApiBuilder.*;

public class UsuarioController {

    private final UsuarioService usuarioService = new UsuarioService();

    public void registrarRutas() {

        // Mostrar formulario de login
        get("/login", ctx -> {
            Map<String, Object> model = new HashMap<>();
            ctx.render("/login.html", model);
        });

        // Mostrar formulario de registro
        get("/registro", ctx -> {
            Map<String, Object> model = new HashMap<>();
            ctx.render("/registro.html", model);
        });

        // Procesar login
        post("/login", this::login);

        // Procesar registro
        post("/registro", this::registro);

        // Cerrar sesión
        get("/logout", ctx -> {
            ctx.sessionAttribute("usuario", null);
            ctx.redirect("/login");
        });

        // Panel admin - listar usuarios
        get("/admin/usuarios", ctx -> {
            if (!esAdmin(ctx)) { ctx.redirect("/login"); return; }
            Map<String, Object> model = new HashMap<>();
            model.put("usuario", ctx.sessionAttribute("usuario"));
            model.put("usuarios", usuarioService.listarTodos());

            // Flash messages
            String flashError   = ctx.sessionAttribute("flashError");
            String flashSuccess = ctx.sessionAttribute("flashSuccess");
            if (flashError   != null) { model.put("flashError",   flashError);   ctx.sessionAttribute("flashError",   null); }
            if (flashSuccess != null) { model.put("flashSuccess", flashSuccess); ctx.sessionAttribute("flashSuccess", null); }


            ctx.render("/admin/usuarios.html", model);
        });

        // Asignar rol organizador
        post("/admin/usuarios/{id}/organizador", ctx -> {
            if (!esAdmin(ctx)) { ctx.redirect("/login"); return; }
            Long id = Long.parseLong(ctx.pathParam("id"));
            usuarioService.asignarRolOrganizador(id);
            ctx.redirect("/admin/usuarios");
        });

        // Revocar rol organizador
        post("/admin/usuarios/{id}/revocar", ctx -> {
            if (!esAdmin(ctx)) { ctx.redirect("/login"); return; }
            Long id = Long.parseLong(ctx.pathParam("id"));
            usuarioService.revocarRolOrganizador(id);
            ctx.redirect("/admin/usuarios");
        });

        // Bloquear usuario
        post("/admin/usuarios/{id}/bloquear", ctx -> {
            if (!esAdmin(ctx)) { ctx.redirect("/login"); return; }
            Long id = Long.parseLong(ctx.pathParam("id"));
            usuarioService.bloquearUsuario(id);
            ctx.redirect("/admin/usuarios");
        });

        // Desbloquear usuario
        post("/admin/usuarios/{id}/desbloquear", ctx -> {
            if (!esAdmin(ctx)) { ctx.redirect("/login"); return; }
            Long id = Long.parseLong(ctx.pathParam("id"));
            usuarioService.desbloquearUsuario(id);
            ctx.redirect("/admin/usuarios");
        });

        // Crear usuario (POST)
        post("/admin/usuarios/crear", ctx -> {
            if (!esAdmin(ctx)) { ctx.redirect("/login"); return; }
            String nombre   = ctx.formParam("nombre");
            String apellido = ctx.formParam("apellido");
            String email    = ctx.formParam("email");
            String password = ctx.formParam("password");
            String rol      = ctx.formParam("rol");

            if (password == null || password.trim().length() < 8) {
                ctx.sessionAttribute("flashError", "La contraseña debe tener mínimo 8 caracteres");
                ctx.redirect("/admin/usuarios");
                return;
            }


            String hash = BCrypt.hashpw(password, BCrypt.gensalt());
            String error = UsuarioService.crearComoAdmin(nombre, apellido, email, hash, rol);

            if (error != null) {
                ctx.sessionAttribute("flashError", error);
            } else {
                ctx.sessionAttribute("flashSuccess", "Usuario creado exitosamente");
            }
            ctx.redirect("/admin/usuarios");
        });

        // Editar usuario (POST)
        post("/admin/usuarios/{id}/editar", ctx -> {
            if (!esAdmin(ctx)) { ctx.redirect("/login"); return; }
            Long id         = Long.parseLong(ctx.pathParam("id"));
            String nombre   = ctx.formParam("nombre");
            String apellido = ctx.formParam("apellido");
            String email    = ctx.formParam("email");
            String rol      = ctx.formParam("rol");
            String password = ctx.formParam("password");

            String error = usuarioService.editarComoAdmin(id, nombre, apellido, email, rol, password);
            if (error != null) {
                ctx.sessionAttribute("flashError", error);
            } else {
                ctx.sessionAttribute("flashSuccess", "Usuario actualizado exitosamente");
            }
            ctx.redirect("/admin/usuarios");
        });

        // Ver perfil
        get("/perfil", ctx -> {
            Usuario u = ctx.sessionAttribute("usuario");
            if (u == null) { ctx.redirect("/login"); return; }
            Usuario fresco = usuarioService.buscarPorId(u.getId());
            Map<String, Object> model = new HashMap<>();
            model.put("usuario", fresco);
            if (fresco.getRol() == TipoUsuario.PARTICIPANTE) {
                model.put("registros", RegistroService.listarPorUsuario(fresco.getId()));
            } else {
                model.put("registros", List.of());
            }
            ctx.render("perfil.html", model);
        });

        // Actualizar nombre y apellido
        post("/perfil/actualizar", ctx -> {
            Usuario u = ctx.sessionAttribute("usuario");
            if (u == null) { ctx.redirect("/login"); return; }
            String nombre = ctx.formParam("nombre");
            String apellido = ctx.formParam("apellido");
            UsuarioDAO.actualizarPerfil(u.getId(), nombre, apellido);
            u.setNombre(nombre);
            u.setApellido(apellido);
            ctx.sessionAttribute("usuario", u);
            ctx.redirect("/perfil?mensaje=Perfil actualizado correctamente");
        });

        // Cambiar contraseña
        post("/perfil/password", ctx -> {
            Usuario u = ctx.sessionAttribute("usuario");
            if (u == null) { ctx.redirect("/login"); return; }

            try {
                String nueva = ctx.formParam("passwordNueva");

                if(nueva == null || nueva.trim().length() < 8){
                    ctx.redirect("/perfil?error=La contraseña debe tener mínimo 8 caracteres");
                    return;
                }

                String hash = BCrypt.hashpw(nueva, BCrypt.gensalt());

                UsuarioDAO.cambiarPassword(u.getId(), hash);

                ctx.redirect("/perfil?mensaje=Contraseña actualizada correctamente");

            } catch (Exception e) {
                ctx.redirect("/perfil?error=Error al cambiar la contraseña");
            }
        });

        // Subir foto
        post("/perfil/foto", ctx -> {
            Usuario u = ctx.sessionAttribute("usuario");
            if (u == null) { ctx.redirect("/login"); return; }
            try {
                var uploadedFile = ctx.uploadedFile("foto");
                if (uploadedFile != null) {
                    byte[] bytes = uploadedFile.content().readAllBytes();
                    String base64 = Base64.getEncoder().encodeToString(bytes);
                    String mimeType = uploadedFile.contentType();
                    if(!mimeType.startsWith("image/")){
                        ctx.redirect("/perfil?error=Archivo inválido");
                        return;
                    }
                    String fotoBase64 = "data:" + mimeType + ";base64," + base64;
                    UsuarioDAO.actualizarFoto(u.getId(), fotoBase64);
                    u.setFotoBase64(fotoBase64);
                    ctx.sessionAttribute("usuario", u);
                }
                ctx.redirect("/perfil?mensaje=Foto actualizada correctamente");
            } catch (Exception e) {
                ctx.redirect("/perfil?error=Error al subir la foto");
            }
        });
    }

    private void login(Context ctx) {
        String email = ctx.formParam("email");
        String password = ctx.formParam("password");
        Usuario usuario = usuarioService.login(email, password);
        if (usuario == null) {
            Map<String, Object> model = new HashMap<>();
            model.put("error", "Credenciales incorrectas o usuario bloqueado");
            ctx.render("/login.html", model);
            return;
        }
        ctx.sessionAttribute("usuario", usuario);
        if (usuario.getRol() == TipoUsuario.ADMINISTRADOR) {
            ctx.redirect("/admin/usuarios");
        } else if (usuario.getRol() == TipoUsuario.ORGANIZADOR) {
            ctx.redirect("/organizador/eventos");
        } else {
            ctx.redirect("/eventos");
        }
    }

    private void registro(Context ctx) {

        String nombre = ctx.formParam("nombre");
        String apellido = ctx.formParam("apellido");
        String email = ctx.formParam("email");
        String password = ctx.formParam("password");

        Map<String, Object> model = new HashMap<>();

        // validar longitud mínima
        if(password == null || password.trim().length() < 8){
            model.put("error", "La contraseña debe tener mínimo 8 caracteres");
            ctx.render("/registro.html", model);
            return;
        }

        String error = usuarioService.registrar(nombre, apellido, email, password);

        if (error != null) {
            model.put("error", error);
            model.put("nombre", nombre);
            model.put("apellido", apellido);
            model.put("email", email);
            ctx.render("/registro.html", model);
            return;
        }

        ctx.redirect("/login");
    }

    // Helpers de sesión
    public static boolean esAdmin(Context ctx) {
        Usuario u = ctx.sessionAttribute("usuario");
        return u != null && u.getRol() == TipoUsuario.ADMINISTRADOR;
    }

    public static boolean esOrganizador(Context ctx) {
        Usuario u = ctx.sessionAttribute("usuario");
        return u != null && (u.getRol() == TipoUsuario.ORGANIZADOR || u.getRol() == TipoUsuario.ADMINISTRADOR);
    }

    public static boolean estaAutenticado(Context ctx) {
        return ctx.sessionAttribute("usuario") != null;
    }

}