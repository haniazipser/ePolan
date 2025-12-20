package com.example.ePolan.Repositories;

import com.example.ePolan.Model.Entities.ExerciseDeclaration;
import com.example.ePolan.Model.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.UUID;

@Repository
public interface DeclarationRepository extends JpaRepository<ExerciseDeclaration, UUID> {
    Set<ExerciseDeclaration> findByStudent(User user);
    Set<ExerciseDeclaration> findByExercise_Id(UUID exerciseId);

    Set<ExerciseDeclaration> findByStudentAndExercise_Lesson_Id(User user, UUID id);

    Integer countByStudentAndExercise_Lesson_Id(User user, UUID id);

    Set<ExerciseDeclaration> findByStudentAndExercise_Lesson_Course_Id(User user, UUID id);

    Set<ExerciseDeclaration> findByExercise_Lesson_Id(UUID id);

}