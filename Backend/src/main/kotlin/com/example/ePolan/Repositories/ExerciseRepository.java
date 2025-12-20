package com.example.ePolan.Repositories;

import com.example.ePolan.Model.Entities.Exercise;
import com.example.ePolan.Model.Entities.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.UUID;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, UUID> {
    Set<Exercise> findByLesson_Id(UUID sessionId);


    Set<Exercise>  findByLesson(Lesson lesson);

    int countByLesson_Id(UUID lessonId);
}