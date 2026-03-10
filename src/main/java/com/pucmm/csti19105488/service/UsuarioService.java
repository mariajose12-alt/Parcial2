package com.pucmm.csti19105488.service;

import com.pucmm.csti19105488.dao.UsuarioDAO;
import com.pucmm.csti19105488.model.enums.TipoUsuario;
import com.pucmm.csti19105488.model.Usuario;
import com.pucmm.csti19105488.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import org.mindrot.jbcrypt.BCrypt;
import java.util.List;

public class UsuarioService {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public void crearAdminInicial() {
        if (!usuarioDAO.existeAdministrador()) {
            Usuario admin = new Usuario(
                    "Admin",
                    "Sistema",
                    "admin@ce.pucmm.edu.do",
                    BCrypt.hashpw("admin123", BCrypt.gensalt()),
                    TipoUsuario.ADMINISTRADOR
            );
            usuarioDAO.guardar(admin);
            System.out.println("Usuario administrador creado: admin@ce.pucmm.edu.do / admin123");
        }
    }

    public Usuario login(String email, String password) {
        Usuario usuario = usuarioDAO.buscarPorEmail(email);
        if (usuario == null) return null;
        if (!usuario.isActivo()) return null;
        if (!BCrypt.checkpw(password, usuario.getPassword())) return null;
        return usuario;
    }

    public String registrar(String nombre, String apellido, String email, String password) {
        if (usuarioDAO.buscarPorEmail(email) != null) {
            return "El email ya está registrado";
        }
        Usuario usuario = new Usuario(
                nombre,
                apellido,
                email,
                BCrypt.hashpw(password, BCrypt.gensalt()),
                TipoUsuario.PARTICIPANTE
        );
        usuarioDAO.guardar(usuario);
        return null;
    }

    public String asignarRolOrganizador(Long usuarioId) {
        Usuario usuario = usuarioDAO.buscarPorId(usuarioId);
        if (usuario == null) return "Usuario no encontrado";
        usuario.setRol(TipoUsuario.ORGANIZADOR);
        usuarioDAO.actualizar(usuario);
        return null;
    }

    public String revocarRolOrganizador(Long usuarioId) {
        Usuario usuario = usuarioDAO.buscarPorId(usuarioId);
        if (usuario == null) return "Usuario no encontrado";
        if (usuario.getRol() == TipoUsuario.ADMINISTRADOR) return "No puedes modificar al administrador";
        usuario.setRol(TipoUsuario.PARTICIPANTE);
        usuarioDAO.actualizar(usuario);
        return null;
    }

    public String bloquearUsuario(Long usuarioId) {
        Usuario usuario = usuarioDAO.buscarPorId(usuarioId);
        if (usuario == null) return "Usuario no encontrado";
        if (usuario.getRol() == TipoUsuario.ADMINISTRADOR) return "No puedes bloquear al administrador";
        usuario.setActivo(false);
        usuarioDAO.actualizar(usuario);
        return null;
    }

    public String desbloquearUsuario(Long usuarioId) {
        Usuario usuario = usuarioDAO.buscarPorId(usuarioId);
        if (usuario == null) return "Usuario no encontrado";
        usuario.setActivo(true);
        usuarioDAO.actualizar(usuario);
        return null;
    }

    public List<Usuario> listarTodos() {
        return usuarioDAO.listarTodos();
    }

    public Usuario buscarPorId(Long id) {
        return usuarioDAO.buscarPorId(id);
    }

    //Creacion y Edicion de usuarios como Administrador
    public static String crearComoAdmin(String nombre, String apellido,
                                        String email, String password, String rolStr) {
        if (nombre == null || nombre.isBlank())   return "El nombre es requerido";
        if (apellido == null || apellido.isBlank()) return "El apellido es requerido";
        if (email == null || email.isBlank())     return "El email es requerido";
        if (password == null || password.length() < 8)
            return "La contraseña debe tener mínimo 8 caracteres";

        if (UsuarioDAO.buscarPorEmail(email) != null)
            return "El email ya está registrado";

        TipoUsuario rol;
        try {
            rol = TipoUsuario.valueOf(rolStr.toUpperCase());
        } catch (Exception e) {
            return "Rol inválido";
        }

        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        Usuario u   = new Usuario(nombre, apellido, email, hash, rol);
        UsuarioDAO.guardar(u);
        return null; // null = sin error
    }

    public String editarComoAdmin(Long id, String nombre, String apellido, String email, String rolStr, String password) {
        Usuario u = usuarioDAO.buscarPorId(id);
        if (u == null) return "Usuario no encontrado";
        if (u.getRol() == TipoUsuario.ADMINISTRADOR) return "No se puede editar al administrador";

        if (nombre == null || nombre.isBlank())   return "El nombre es requerido";
        if (apellido == null || apellido.isBlank()) return "El apellido es requerido";
        if (email == null || email.isBlank())     return "El email es requerido";

        // Verificar email duplicado en otro usuario
        Usuario porEmail = usuarioDAO.buscarPorEmail(email);
        if (porEmail != null && !porEmail.getId().equals(id)) {
            return "El email ya está en uso por otro usuario";
        }

        TipoUsuario rol;
        try {
            rol = TipoUsuario.valueOf(rolStr.toUpperCase());
        } catch (Exception e) {
            return "Rol inválido";
        }

        u.setNombre(nombre);
        u.setApellido(apellido);
        u.setEmail(email);
        u.setRol(rol);

        if (password != null && !password.isBlank()) {
            if (password.length() < 8) return "La contraseña debe tener mínimo 8 caracteres";
            u.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
        }

        usuarioDAO.actualizar(u);
        return null;
    }

}