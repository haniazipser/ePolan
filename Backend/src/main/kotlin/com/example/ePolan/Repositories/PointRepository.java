package com.example.ePolan.Repositories;

import com.example.ePolan.Model.Entities.Point;
import com.example.ePolan.Model.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface PointRepository extends JpaRepository<Point, UUID> {
    Set<Point> findByStudent(User user);

    Set<Point> findByStudentAndLesson_Course_Id(User user, UUID courseId);

    Optional<Point> findByLesson_IdAndStudent(UUID lessonId, User user);
}