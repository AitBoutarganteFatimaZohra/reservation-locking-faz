package com.faz.locking.service;

import com.faz.locking.modele.ReservationFaz;

import javax.persistence.OptimisticLockException;
import javax.persistence.RollbackException;
import java.util.function.Consumer;

/**
 * Gestionnaire de retry pour les conflits OptimisticLockException.
 */
public class ReservationFazRetryHandler {

    private static final int MAX_RETRIES_FAZ = 3;
    private final ReservationFazService serviceFaz;

    public ReservationFazRetryHandler(ReservationFazService serviceFaz) {
        this.serviceFaz = serviceFaz;
    }

    public ReservationFaz modifierAvecRetryFaz(Long idReservationFaz, Consumer<ReservationFaz> modificationFaz) {
        int tentativeFaz = 0;

        while (tentativeFaz < MAX_RETRIES_FAZ) {
            try {
                ReservationFaz reservationFaz = serviceFaz.trouverParIdFaz(idReservationFaz);
                if (reservationFaz == null) {
                    throw new IllegalArgumentException("Reservation non trouvee : " + idReservationFaz);
                }

                modificationFaz.accept(reservationFaz);

                ReservationFaz resultatFaz = serviceFaz.modifierReservationFaz(reservationFaz);

                if (tentativeFaz > 0) {
                    System.out.println("✅ Modification reussie apres " + tentativeFaz + " retry(ies).");
                }
                return resultatFaz;

            } catch (OptimisticLockException | RollbackException e) {
                tentativeFaz++;
                System.err.println("⚠️ Conflit detecte (tentative " + tentativeFaz + "/" + MAX_RETRIES_FAZ + ") : "
                        + e.getClass().getSimpleName());

                if (tentativeFaz >= MAX_RETRIES_FAZ) {
                    throw new IllegalStateException(
                            "Echec apres " + MAX_RETRIES_FAZ + " tentatives. La reservation a ete modifiee par un autre utilisateur.",
                            e);
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrompu", ie);
                }
            }
        }

        throw new IllegalStateException("Impossible de modifier la reservation apres " + MAX_RETRIES_FAZ + " tentatives.");
    }
}