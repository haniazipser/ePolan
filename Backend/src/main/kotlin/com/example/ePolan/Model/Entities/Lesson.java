package com.example.ePolan.Model.Entities;

import com.example.ePolan.Model.Dtos.LessonStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter @Setter
@Table(name = "lesson")
public class Lesson {
    @Id
    private UUID id;
    private Instant classDate;
    @ManyToOne
    private Course course;
    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Exercise> lessonExercises;

    @PrePersist
    public void generateId() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }

    public Lesson(){}

    public LessonStatus getLessonStatus() {
        if (classDate.isBefore(Instant.now())){
            return LessonStatus.PAST;
        } if ( Duration.between(Instant.now(), classDate).toDays() <= 7){
            return LessonStatus.NEAR;
        } else {
            return LessonStatus.FUTURE;
        }
    }

    public long getHoursToLesson(){
        return Duration.between(Instant.now(), classDate).toHours();
    }
}
