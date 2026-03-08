package com.pucmm.csti19105488.dao;

import com.pucmm.csti19105488.model.Usuario;
import com.pucmm.csti19105488.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

public class UsuarioDAO {

    public void guardar(Usuario usuario) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(usuario);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void actualizar(Usuario usuario) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(usuario);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public Usuario buscarPorId(Long id) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.find(Usuario.class, id);
        } finally {
            em.close();
        }
    }

    public Usuario buscarPorEmail(String email) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery("SELECT u FROM Usuario u WHERE u.email = :email", Usuario.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public List<Usuario> listarTodos() {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery("SELECT u FROM Usuario u", Usuario.class).getResultList();
        } finally {
            em.close();
        }
    }

    public boolean existeAdministrador() {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            Long count = em.createQuery(
                            "SELECT COUNT(u) FROM Usuario u WHERE u.rol = com.pucmm.csti19105488.model.enums.TipoUsuario.ADMINISTRADOR", Long.class)
                    .getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }

    public static void actualizarPerfil(Long id, String nombre, String apellido) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            Usuario u = em.find(Usuario.class, id);
            u.setNombre(nombre);
            u.setApellido(apellido);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public static void cambiarPassword(Long id, String passwordNueva) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            Usuario u = em.find(Usuario.class, id);
            u.setPassword(BCrypt.hashpw(passwordNueva, BCrypt.gensalt()));
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public static void actualizarFoto(Long id, String fotoBase64) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            Usuario u = em.find(Usuario.class, id);
            u.setFotoBase64(fotoBase64);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}