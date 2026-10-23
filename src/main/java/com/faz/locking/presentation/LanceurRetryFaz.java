package com.faz.locking.presentation;

import com.faz.locking.modele.ReservationFaz;
import com.faz.locking.service.ReservationFazRetryHandler;
import com.faz.locking.service.ReservationFazService;
import com.faz.locking.service.ReservationFazServiceImpl;

import java.time.LocalDateTime;

/**
 * Test du gestionnaire de retry.
 */
public class LanceurRetryFaz {

    public static void main(String[] args) {
        System.out.println("=== TEST DU RETRY HANDLER ===\n");

        ReservationFazService serviceFaz = new ReservationFazServiceImpl();
        ReservationFazRetryHandler retryHandlerFaz = new ReservationFazRetryHandler(serviceFaz);

        // 1. Creer une reservation
        ReservationFaz reservationFaz = new ReservationFaz(
                "Salle C",
                LocalDateTime.of(2025, 3, 1, 14, 0),
                LocalDateTime.of(2025, 3, 1, 16, 0)
        );
        serviceFaz.creerReservationFaz(reservationFaz);
        Long idFaz = reservationFaz.getId();
        System.out.println("Reservation creee : " + reservationFaz + "\n");

        // 2. Un "utilisateur concurrent" modifie la reservation
        System.out.println("--- Un autre utilisateur modifie la reservation ---");
        ReservationFaz reservationConcurrenteFaz = serviceFaz.trouverParIdFaz(idFaz);
        reservationConcurrenteFaz.setSalleFaz("Salle C (modifiee par concurrent)");
        serviceFaz.modifierReservationFaz(reservationConcurrenteFaz);
        System.out.println("Modification concurrente appliquee. Version = 1\n");

        // 3. Le retry handler tente de modifier la reservation
        System.out.println("--- Retry handler tente de modifier la reservation ---");

        try {
            ReservationFaz resultatFaz = retryHandlerFaz.modifierAvecRetryFaz(idFaz, (r) -> {
                r.setSalleFaz("Salle C (modifiee par retry)");
            });

            System.out.println("\n✅ Resultat final : " + resultatFaz);
        } catch (IllegalStateException e) {
            System.err.println("\n❌ Echec du retry : " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("  Cause : " + e.getCause().getClass().getSimpleName());
            }
        }

        // 4. Verification finale
        System.out.println("\n--- Verification finale ---");
        ReservationFaz finaleFaz = serviceFaz.trouverParIdFaz(idFaz);
        System.out.println("Etat final : " + finaleFaz);

        ReservationFazServiceImpl.fermerFaz();
        System.out.println("\n=== FIN ===");
    }
}