package com.faz.locking.presentation;

import com.faz.locking.dao.ReservationFazDao;
import com.faz.locking.modele.ReservationFaz;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.OptimisticLockException;
import javax.persistence.Persistence;
import javax.persistence.RollbackException;
import java.time.LocalDateTime;

/**
 * Simule un conflit concurrent avec 2 EntityManager.
 *
 * Scenario :
 * 1. Le client A charge la reservation (version 0)
 * 2. Le client B charge la MEME reservation (version 0)
 * 3. Le client A modifie et commit -> la version passe a 1
 * 4. Le client B modifie et commit -> OptimisticLockException / RollbackException
 */
public class LanceurConflitFaz {

    public static void main(String[] args) {
        System.out.println("=== SIMULATION DE CONFLIT CONCURRENT ===\n");

        EntityManagerFactory emfFaz = Persistence.createEntityManagerFactory("UniteReservationFaz");

        // ============================================================
        // 1. Creation d'une reservation initiale
        // ============================================================
        EntityManager emInitFaz = emfFaz.createEntityManager();
        ReservationFazDao daoInitFaz = new ReservationFazDao();

        ReservationFaz reservationInitialeFaz = new ReservationFaz(
                "Salle A",
                LocalDateTime.of(2025, 1, 1, 10, 0),
                LocalDateTime.of(2025, 1, 1, 12, 0)
        );
        daoInitFaz.enregistrerFaz(emInitFaz, reservationInitialeFaz);
        Long idReservationFaz = reservationInitialeFaz.getId();
        emInitFaz.close();

        System.out.println("Reservation creee : ID = " + idReservationFaz
                + ", version = " + reservationInitialeFaz.getVersionFaz() + "\n");

        // ============================================================
        // 2. CLIENT A : charge la reservation (version 0)
        // ============================================================
        EntityManager emA_Faz = emfFaz.createEntityManager();
        ReservationFazDao daoA_Faz = new ReservationFazDao();
        ReservationFaz reservationA_Faz = daoA_Faz.rechercherParIdFaz(emA_Faz, idReservationFaz);

        System.out.println("CLIENT A charge la reservation : version = " + reservationA_Faz.getVersionFaz());

        // ============================================================
        // 3. CLIENT B : charge la MEME reservation (version 0)
        // ============================================================
        EntityManager emB_Faz = emfFaz.createEntityManager();
        ReservationFazDao daoB_Faz = new ReservationFazDao();
        ReservationFaz reservationB_Faz = daoB_Faz.rechercherParIdFaz(emB_Faz, idReservationFaz);

        System.out.println("CLIENT B charge la reservation : version = " + reservationB_Faz.getVersionFaz() + "\n");

        // ============================================================
        // 4. CLIENT A : modifie et commit (la version passe a 1)
        // ============================================================
        System.out.println("--- CLIENT A modifie la salle en 'Salle A (modifiee par A)' ---");
        reservationA_Faz.setSalleFaz("Salle A (modifiee par A)");
        daoA_Faz.modifierFaz(emA_Faz, reservationA_Faz);
        emA_Faz.close();

        System.out.println("CLIENT A a commit. La version est maintenant : 1\n");

        // ============================================================
        // 5. CLIENT B : modifie et commit -> CONFLIT DETECTE
        // ============================================================
        System.out.println("--- CLIENT B modifie la salle en 'Salle A (modifiee par B)' ---");
        reservationB_Faz.setSalleFaz("Salle A (modifiee par B)");

        try {
            daoB_Faz.modifierFaz(emB_Faz, reservationB_Faz);
            System.out.println("CLIENT B a commit (pas d'erreur).");
        } catch (OptimisticLockException e) {
            System.err.println("\n❌ CONFLIT DETECTE : OptimisticLockException !");
            System.err.println("   Le client B a essaye de modifier une reservation");
            System.err.println("   qui a deja ete modifiee par le client A.");
            System.err.println("   Message : " + e.getMessage());
        } catch (RollbackException e) {
            // Hibernate encapsule parfois l'OptimisticLockException dans une RollbackException
            System.err.println("\n❌ CONFLIT DETECTE : RollbackException (cause : conflit de version)");
            System.err.println("   Le client B a essaye de modifier une reservation");
            System.err.println("   qui a deja ete modifiee par le client A.");
            Throwable causeFaz = e.getCause();
            if (causeFaz != null) {
                System.err.println("   Cause : " + causeFaz.getClass().getSimpleName()
                        + " - " + causeFaz.getMessage());
                if (causeFaz.getCause() != null) {
                    System.err.println("   Cause profonde : " + causeFaz.getCause().getClass().getSimpleName()
                            + " - " + causeFaz.getCause().getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("\n❌ ERREUR : " + e.getClass().getSimpleName());
            System.err.println("   Message : " + e.getMessage());
        } finally {
            if (emB_Faz.isOpen()) emB_Faz.close();
        }

        // ============================================================
        // 6. Verification finale : qui a gagne ?
        // ============================================================
        System.out.println("\n--- Verification finale ---");
        EntityManager emVerifFaz = emfFaz.createEntityManager();
        ReservationFaz reservationFinaleFaz = emVerifFaz.find(ReservationFaz.class, idReservationFaz);
        System.out.println("Etat final : " + reservationFinaleFaz);
        emVerifFaz.close();

        emfFaz.close();
        System.out.println("\n=== FIN ===");
    }
}