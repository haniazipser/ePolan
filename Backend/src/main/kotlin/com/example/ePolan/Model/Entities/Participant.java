package com.example.ePolan.Model.Entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter @Setter
@Table (name = "participant")
public class Participant {
    @Id
    UUID id;
    private User student;
    @ManyToOne
    private Course course;

    @Enumerated(EnumType.STRING)
    private InvitationStatus invitationStatus;

    public Participant(){};
    @PrePersist
    public void generateId() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
