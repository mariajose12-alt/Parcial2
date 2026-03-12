package com.pucmm.csti19105488.dao;

import com.pucmm.csti19105488.model.CodigoQR;
import com.pucmm.csti19105488.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

public class CodigoQRDAO {

    public void guardar(CodigoQR codigoQR) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(codigoQR);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public CodigoQR buscarPorToken(String token) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT q FROM CodigoQR q WHERE q.validacionToken = :token", CodigoQR.class)
                    .setParameter("token", token)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public static CodigoQR buscarPorRegistro(Long registroId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT q FROM CodigoQR q WHERE q.registro.id = :id", CodigoQR.class)
                    .setParameter("id", registroId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public void eliminarPorRegistro(Long id) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();

            em.createQuery("DELETE FROM CodigoQR q WHERE q.registro.id = :id")
                    .setParameter("id", id)
                    .executeUpdate();

            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}