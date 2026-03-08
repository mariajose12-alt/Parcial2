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

}