package com.faz.locking.service;

import com.faz.locking.modele.ReservationFaz;

import java.util.List;

/**
 * Contrat du service metier pour la gestion des reservations.
 */
public interface ReservationFazService {

    /**
     * Cree une nouvelle reservation.
     */
    ReservationFaz creerReservationFaz(ReservationFaz reservationFaz);

    /**
     * Modifie une reservation existante.
     * Peut lever une OptimisticLockException en cas de conflit.
     */
    ReservationFaz modifierReservationFaz(ReservationFaz reservationFaz);

    /**
     * Recherche une reservation par son ID.
     */
    ReservationFaz trouverParIdFaz(Long idFaz);

    /**
     * Liste toutes les reservations.
     */
    List<ReservationFaz> listerReservationsFaz();
}