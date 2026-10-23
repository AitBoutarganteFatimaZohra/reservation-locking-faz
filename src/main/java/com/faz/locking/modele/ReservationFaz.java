package com.faz.locking.modele;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservation_faz")
public class ReservationFaz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reservation_faz")
    private Long id;

    @Column(name = "salle_faz", nullable = false)
    private String salleFaz;

    @Column(name = "date_debut_faz", nullable = false)
    private LocalDateTime dateDebutFaz;

    @Column(name = "date_fin_faz", nullable = false)
    private LocalDateTime dateFinFaz;

    @Version
    @Column(name = "version_faz")
    private int versionFaz;

    public ReservationFaz() {}

    public ReservationFaz(String salleFaz, LocalDateTime dateDebutFaz, LocalDateTime dateFinFaz) {
        this.salleFaz = salleFaz;
        this.dateDebutFaz = dateDebutFaz;
        this.dateFinFaz = dateFinFaz;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSalleFaz() { return salleFaz; }
    public void setSalleFaz(String salleFaz) { this.salleFaz = salleFaz; }

    public LocalDateTime getDateDebutFaz() { return dateDebutFaz; }
    public void setDateDebutFaz(LocalDateTime dateDebutFaz) { this.dateDebutFaz = dateDebutFaz; }

    public LocalDateTime getDateFinFaz() { return dateFinFaz; }
    public void setDateFinFaz(LocalDateTime dateFinFaz) { this.dateFinFaz = dateFinFaz; }

    public int getVersionFaz() { return versionFaz; }
    public void setVersionFaz(int versionFaz) { this.versionFaz = versionFaz; }

    @Override
    public String toString() {
        return "ReservationFaz{id=" + id
                + ", salle='" + salleFaz + "'"
                + ", debut=" + dateDebutFaz
                + ", version=" + versionFaz + "}";
    }
}