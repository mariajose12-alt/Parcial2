package com.pucmm.csti19105488.dao;

import com.pucmm.csti19105488.model.enums.EstadoEvento;
import com.pucmm.csti19105488.model.enums.TipoEvento;
import com.pucmm.csti19105488.model.Evento;
import com.pucmm.csti19105488.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;


public class EventoDAO {

    public void guardar(Evento evento) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(evento);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void actualizar(Evento evento) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(evento);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void eliminar(Long id) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            Evento evento = em.find(Evento.class, id);
            if (evento != null) em.remove(evento);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public Evento buscarPorId(Long id) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.find(Evento.class, id);
        } finally {
            em.close();
        }
    }

    public List<Evento> listarTodos() {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery("SELECT e FROM Evento e", Evento.class).getResultList();
        } finally {
            em.close();
        }
    }

    public List<Evento> listarPublicados() {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT e FROM Evento e WHERE e.estado = :estado", Evento.class)
                    .setParameter("estado", EstadoEvento.PUBLICADO)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Evento> listarPorOrganizador(Long organizadorId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT e FROM Evento e WHERE e.organizador.id = :id", Evento.class)
                    .setParameter("id", organizadorId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public boolean existeEvento(String titulo,
                                LocalDateTime fechaInicio,
                                LocalDateTime fechaFin,
                                TipoEvento tipo,
                                String lugar) {

        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();

        try {

            Long count = em.createQuery(
                            "SELECT COUNT(e) FROM Evento e " +
                                    "WHERE lower(e.titulo) = :titulo " +
                                    "AND e.fechaInicio = :inicio " +
                                    "AND e.fechaFin = :fin " +
                                    "AND e.tipo = :tipo " +
                                    "AND lower(e.lugar) = :lugar",
                            Long.class
                    )
                    .setParameter("titulo", titulo.toLowerCase())
                    .setParameter("inicio", fechaInicio)
                    .setParameter("fin", fechaFin)
                    .setParameter("tipo", tipo)
                    .setParameter("lugar", lugar.toLowerCase())
                    .getSingleResult();

            return count > 0;

        } finally {
            em.close();
        }
    }
}