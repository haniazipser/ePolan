package com.example.ePolan.Model.Entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@Entity
@Getter @Setter
@Table(name = "exercise")
public class Exercise {
    @Id
    private UUID id;
    @ManyToOne
    private Lesson lesson;
    private Integer exerciseNumber;
    private String subpoint;

    @OneToMany(mappedBy = "exercise")
    private Set<ExerciseDeclaration> declarations;

    private String approvedStudent;

    public Exercise(){}

    @PrePersist
    public void generateId() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }

}
