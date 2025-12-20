package com.example.ePolan.Model.Entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.Set;
import java.util.UUID;

@Getter @Setter
@Entity
@Table(name = "course")
public class Course {
    @Id
    private UUID id;
    private String name;
    private String  instructor;
    private String creator;
    @ElementCollection
    private Set<LessonTime> lessonTimes;

    @OneToMany(mappedBy = "course",cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Lesson> lessons;

    //private Set<String> students;
    @OneToMany(mappedBy = "course")
    private Set<Participant> students;
    private Instant startDate;

    private Instant endDate;
    private Integer frequency;

    private String courseCode;

    @PrePersist
    public void generateId() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }


    public Course(){}

    public Instant getFirstLesson(LessonTime lessonTime) {

        ZoneId zone = ZoneId.systemDefault();

        LocalDate date = startDate.atZone(zone).toLocalDate();

        LocalDate nextLessonDate = date.with(TemporalAdjusters.next(lessonTime.getDayOfWeek()));

        Instant nextLessonInstant = nextLessonDate.atStartOfDay(zone).toInstant();

        return nextLessonInstant;
    }

    public boolean isStudentAMemeber(String email){
        boolean found = false;
        for (Participant student : students){
            if (student.getEmail().toLowerCase().equals(email)){
                found = true;
                break;
            }
        }
        return found;
    }

}
