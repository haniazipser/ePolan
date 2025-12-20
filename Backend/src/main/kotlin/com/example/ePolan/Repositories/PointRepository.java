package com.example.ePolan.Repositories;

import com.example.ePolan.Model.Entities.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface PointRepository extends JpaRepository<Point, UUID> {
    Set<Point> findByStudent(String email);

    Set<Point> findByStudentAndLesson_Course_Id(String email, UUID courseId);

    Optional<Point> findByLesson_IdAndStudent(UUID lessonId, String email);
}