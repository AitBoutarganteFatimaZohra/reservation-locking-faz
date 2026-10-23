package com.faz.locking.presentation;

import com.faz.locking.modele.ReservationFaz;
import com.faz.locking.service.ReservationFazService;
import com.faz.locking.service.ReservationFazServiceImpl;

import java.time.LocalDateTime;
import java.util.List;

public class LanceurServiceFaz {

    public static void main(String[] args) {
        System.out.println("=== TEST DU SERVICE DE RESERVATIONS ===\n");

        ReservationFazService serviceFaz = new ReservationFazServiceImpl();

        // 1. Creer une reservation
        ReservationFaz reservationFaz = new ReservationFaz(
                "Salle B",
                LocalDateTime.of(2025, 2, 1, 9, 0),
                LocalDateTime.of(2025, 2, 1, 11, 0)
        );
        serviceFaz.creerReservationFaz(reservationFaz);
        System.out.println("Reservation creee : " + reservationFaz + "\n");

        // 2. Rechercher par ID
        ReservationFaz trouveeFaz = serviceFaz.trouverParIdFaz(reservationFaz.getId());
        System.out.println("Recherche par ID : " + trouveeFaz + "\n");

        // 3. Modifier la reservation
        trouveeFaz.setSalleFaz("Salle B (modifiee)");
        serviceFaz.modifierReservationFaz(trouveeFaz);
        System.out.println("Reservation modifiee.\n");

        // 4. Lister toutes les reservations
        List<ReservationFaz> toutesFaz = serviceFaz.listerReservationsFaz();
        System.out.println("Liste des reservations :");
        for (ReservationFaz r : toutesFaz) {
            System.out.println("  " + r);
        }

        // 5. Fermer la SessionFactory
        ReservationFazServiceImpl.fermerFaz();
        System.out.println("\n=== FIN ===");
    }
}