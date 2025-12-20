package com.example.ePolan.Model.Entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter @Setter
@Table(name = "point")
public class Point {
    @Id
    private UUID id;
    private String student;
    @ManyToOne
    private Lesson lesson;
    private Double activityValue;//jesli zmieniam nazwe tutaj
    public Point(){};

    @PrePersist
    public void generateId() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }

}
