package com.pucmm.csti19105488.dao;

import com.pucmm.csti19105488.model.Registro;
import com.pucmm.csti19105488.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.time.LocalDate;
import java.util.List;

public class RegistroDAO {

    public void guardar(Registro registro) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(registro);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void actualizar(Registro registro) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(registro);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void eliminar(Long id) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            Registro r = em.find(Registro.class, id);
            if (r != null) em.remove(r);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public Registro buscarPorId(Long id) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.find(Registro.class, id);
        } finally {
            em.close();
        }
    }

    public Registro buscarPorToken(String token) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT r FROM Registro r WHERE r.qrToken = :token", Registro.class)
                    .setParameter("token", token)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public static boolean existeInscripcion(Long usuarioId, Long eventoId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            Long count = em.createQuery(
                            "SELECT COUNT(r) FROM Registro r WHERE r.participante.id = :uid AND r.evento.id = :eid", Long.class)
                    .setParameter("uid", usuarioId)
                    .setParameter("eid", eventoId)
                    .getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }

    public List<Registro> listarPorEvento(Long eventoId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT r FROM Registro r WHERE r.evento.id = :id", Registro.class)
                    .setParameter("id", eventoId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Registro> listarPorUsuario(Long usuarioId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT r FROM Registro r WHERE r.participante.id = :id", Registro.class)
                    .setParameter("id", usuarioId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Registro> listarTodos() {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery("SELECT r FROM Registro r", Registro.class).getResultList();
        } finally {
            em.close();
        }
    }

    public List<Registro> listarPorOrganizador(Long organizadorId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT r FROM Registro r WHERE r.evento.organizador.id = :id", Registro.class)
                    .setParameter("id", organizadorId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public static boolean existeAsistenciaHoy(Long registroId, LocalDate fecha) {

        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();

        try {

            Long count = em.createQuery(
                            "SELECT COUNT(a) FROM Asistencia a " +
                                    "WHERE a.registro.id = :rid AND a.fecha = :fecha",
                            Long.class)
                    .setParameter("rid", registroId)
                    .setParameter("fecha", fecha)
                    .getSingleResult();

            return count > 0;

        } finally {
            em.close();
        }
    }


}