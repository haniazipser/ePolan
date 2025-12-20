package com.example.ePolan.Services;

import com.example.ePolan.Model.Dtos.PointDto;
import com.example.ePolan.Model.Entities.Lesson;
import com.example.ePolan.Model.Entities.Point;
import com.example.ePolan.Repositories.LessonRepository;
import com.example.ePolan.Repositories.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PointService {
    private final PointRepository pointRepository;
    private final LessonRepository lessonRepository;

    public List<PointDto> getUsersActivity (String email){
        return pointRepository.findByStudent(email)
                .stream().map(a -> new PointDto(a)).sorted(Comparator.comparing(PointDto::getId)).collect(Collectors.toList());
    }
    public List<PointDto> getUsersActivityInCourse(String email, UUID courseId) {
        return pointRepository.findByStudentAndLesson_Course_Id(email, courseId).stream()
                .sorted(Comparator.comparing(p -> p.getLesson().getClassDate()))
                .map(a -> new PointDto(a)).collect(Collectors.toList());
    }

    public void addStudentActivity(String email, UUID lessonId, Double value) {
        Optional<Point> p = pointRepository.findByLesson_IdAndStudent(lessonId,email);
        if(p.isEmpty()) {
            System.out.println("towrze nowy");
            Point newPoint = new Point();
            newPoint.setActivityValue(value);
            newPoint.setStudent(email);
            Optional<Lesson> lesson = lessonRepository.findById(lessonId);
            if (lesson.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found");
            }
            newPoint.setLesson(lesson.get());
            pointRepository.save(newPoint);
        }else{
            System.out.println("modyfikuje poprzedni o wartości" + value);
            Point point = p.get();
            point.setActivityValue(value);
            pointRepository.save(point);
        }

    }
}
