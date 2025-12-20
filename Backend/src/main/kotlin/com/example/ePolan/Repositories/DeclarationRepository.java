package com.example.ePolan.Repositories;

import com.example.ePolan.Model.Entities.ExerciseDeclaration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.UUID;

@Repository
public interface DeclarationRepository extends JpaRepository<ExerciseDeclaration, UUID> {
    Set<ExerciseDeclaration> findByStudent(String email);
    Set<ExerciseDeclaration> findByExercise_Id(UUID exerciseId);

    Set<ExerciseDeclaration> findByStudentAndExercise_Lesson_Id(String email, UUID id);

    Integer countByStudentAndExercise_Lesson_Id(String email, UUID id);

    Set<ExerciseDeclaration> findByStudentAndExercise_Lesson_Course_Id(String email, UUID id);

    Set<ExerciseDeclaration> findByExercise_Lesson_Id(UUID id);

}