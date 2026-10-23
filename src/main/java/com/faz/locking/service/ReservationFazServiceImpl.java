package com.faz.locking.service;

import com.faz.locking.modele.ReservationFaz;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.OptimisticLockException;
import javax.persistence.Persistence;
import java.util.List;

/**
 * Implementation du service de gestion des reservations.
 *
 * NOTE : On utilise directement l'entite (detached) passee en parametre
 * pour simuler le conflit (comme dans le LanceurConflitFaz).
 * Hibernate compare la version de l'entite detached avec la version en base
 * lors du merge().
 */
public class ReservationFazServiceImpl implements ReservationFazService {

    private static final EntityManagerFactory EMF_FAZ = Persistence.createEntityManagerFactory("UniteReservationFaz");

    @Override
    public ReservationFaz creerReservationFaz(ReservationFaz reservationFaz) {
        EntityManager emFaz = EMF_FAZ.createEntityManager();
        try {
            emFaz.getTransaction().begin();
            emFaz.persist(reservationFaz);
            emFaz.getTransaction().commit();
            return reservationFaz;
        } catch (RuntimeException e) {
            if (emFaz.getTransaction().isActive()) {
                emFaz.getTransaction().rollback();
            }
            throw e;
        } finally {
            emFaz.close();
        }
    }

    @Override
    public ReservationFaz modifierReservationFaz(ReservationFaz reservationFaz) {
        EntityManager emFaz = EMF_FAZ.createEntityManager();
        try {
            emFaz.getTransaction().begin();
            // On utilise merge() directement avec l'entite detached
            // Hibernate va comparer la version et lever OptimisticLockException si conflit
            ReservationFaz fusionFaz = emFaz.merge(reservationFaz);
            emFaz.getTransaction().commit();
            return fusionFaz;
        } catch (OptimisticLockException e) {
            if (emFaz.getTransaction().isActive()) {
                emFaz.getTransaction().rollback();
            }
            throw e;
        } catch (RuntimeException e) {
            if (emFaz.getTransaction().isActive()) {
                emFaz.getTransaction().rollback();
            }
            throw e;
        } finally {
            emFaz.close();
        }
    }

    @Override
    public ReservationFaz trouverParIdFaz(Long idFaz) {
        EntityManager emFaz = EMF_FAZ.createEntityManager();
        try {
            return emFaz.find(ReservationFaz.class, idFaz);
        } finally {
            emFaz.close();
        }
    }

    @Override
    public List<ReservationFaz> listerReservationsFaz() {
        EntityManager emFaz = EMF_FAZ.createEntityManager();
        try {
            return emFaz.createQuery("SELECT r FROM ReservationFaz r", ReservationFaz.class).getResultList();
        } finally {
            emFaz.close();
        }
    }

    /**
     * Ferme l'EntityManagerFactory (a appeler a l'arret de l'application).
     */
    public static void fermerFaz() {
        if (EMF_FAZ != null && EMF_FAZ.isOpen()) {
            EMF_FAZ.close();
        }
    }
}