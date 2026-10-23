package com.faz.locking.presentation;

import com.faz.locking.modele.ReservationFaz;
import com.faz.locking.service.ReservationFazRetryHandler;
import com.faz.locking.service.ReservationFazService;
import com.faz.locking.service.ReservationFazServiceImpl;

import java.time.LocalDateTime;

/**
 * Test de la strategie de retry avec simulation concurrente.
 *
 * Scenario :
 * 1. Creer une reservation
 * 2. Charger 2 copies (tx1 et tx2) avec la meme version
 * 3. tx1 modifie et commit -> version passe a 1
 * 4. tx2 tente de modifier avec le retry handler
 *    -> Le retry handler recharge la version a jour et reussit
 */
public class ConcurrentReservationWithRetryFaz {

    public static void main(String[] args) {
        System.out.println("=== TEST RETRY AVEC SIMULATION CONCURRENTE ===\n");

        ReservationFazService serviceFaz = new ReservationFazServiceImpl();
        ReservationFazRetryHandler retryHandlerFaz = new ReservationFazRetryHandler(serviceFaz);

        // ============================================================
        // 1. Creer une reservation initiale
        // ============================================================
        ReservationFaz reservationFaz = new ReservationFaz(
                "Salle A",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2)
        );
        reservationFaz = serviceFaz.creerReservationFaz(reservationFaz);
        Long idReservationFaz = reservationFaz.getId();

        System.out.println("Reservation creee : " + reservationFaz + "\n");

        // ============================================================
        // 2. Charger 2 copies (simule 2 transactions)
        // ============================================================
        ReservationFaz tx1Faz = serviceFaz.trouverParIdFaz(idReservationFaz);
        ReservationFaz tx2Faz = serviceFaz.trouverParIdFaz(idReservationFaz);

        System.out.println("tx1 charge : version = " + tx1Faz.getVersionFaz());
        System.out.println("tx2 charge : version = " + tx2Faz.getVersionFaz() + "\n");

        // ============================================================
        // 3. Transaction 1 modifie et commit (version -> 1)
        // ============================================================
        System.out.println("--- Transaction 1 modifie la salle en 'Salle B' ---");
        tx1Faz.setSalleFaz("Salle B");
        serviceFaz.modifierReservationFaz(tx1Faz);
        System.out.println("Transaction 1 : modification reussie. Version = 1\n");

        // ============================================================
        // 4. Transaction 2 tente de modifier via le retry handler
        // ============================================================
        System.out.println("--- Transaction 2 tente de modifier via le retry handler ---");

        try {
            ReservationFaz resultatFaz = retryHandlerFaz.modifierAvecRetryFaz(
                    idReservationFaz,
                    (r) -> r.setSalleFaz("Salle C")
            );
            System.out.println("Transaction 2 : modification reussie apres retry.");
            System.out.println("  Resultat : " + resultatFaz);
        } catch (Exception e) {
            System.err.println("Transaction 2 : echec apres retries - " + e.getMessage());
        }

        // ============================================================
        // 5. Verification finale
        // ============================================================
        System.out.println("\n--- Verification finale ---");
        ReservationFaz finaleFaz = serviceFaz.trouverParIdFaz(idReservationFaz);
        System.out.println("Etat final : " + finaleFaz);

        ReservationFazServiceImpl.fermerFaz();
        System.out.println("\n=== FIN ===");
    }
}