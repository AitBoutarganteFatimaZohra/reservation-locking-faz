package com.faz.locking.dao;

import com.faz.locking.modele.ReservationFaz;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

/**
 * DAO pour l'entite ReservationFaz.
 * Gere les operations CRUD et le verrouillage optimiste via @Version.
 */
public class ReservationFazDao {

    /**
     * Persiste une nouvelle reservation.
     */
    public void enregistrerFaz(EntityManager gestionnaireEntiteFaz, ReservationFaz reservationFaz) {
        EntityTransaction txFaz = gestionnaireEntiteFaz.getTransaction();
        try {
            txFaz.begin();
            gestionnaireEntiteFaz.persist(reservationFaz);
            txFaz.commit();
        } catch (RuntimeException e) {
            if (txFaz.isActive()) txFaz.rollback();
            throw e;
        }
    }

    /**
     * Recherche une reservation par son ID.
     */
    public ReservationFaz rechercherParIdFaz(EntityManager gestionnaireEntiteFaz, Long idFaz) {
        return gestionnaireEntiteFaz.find(ReservationFaz.class, idFaz);
    }

    /**
     * Met a jour une reservation.
     * Si une autre transaction a modifie la reservation entre-temps,
     * une OptimisticLockException sera levee au commit.
     */
    public ReservationFaz modifierFaz(EntityManager gestionnaireEntiteFaz, ReservationFaz reservationFaz) {
        EntityTransaction txFaz = gestionnaireEntiteFaz.getTransaction();
        try {
            txFaz.begin();
            ReservationFaz fusionFaz = gestionnaireEntiteFaz.merge(reservationFaz);
            txFaz.commit();
            return fusionFaz;
        } catch (RuntimeException e) {
            if (txFaz.isActive()) txFaz.rollback();
            throw e;
        }
    }
}