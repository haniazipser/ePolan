package com.example.ePolan.Services;

import com.example.ePolan.Model.Dtos.PointDto;
import com.example.ePolan.Model.Entities.Lesson;
import com.example.ePolan.Model.Entities.Point;
import com.example.ePolan.Model.Entities.User;
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
    private final UserService userService;

    public List<PointDto> getUsersActivity (){
        User loggedUser = userService.getLoggedUser();
        return pointRepository.findByStudent(loggedUser)
                .stream().map(a -> new PointDto(a)).collect(Collectors.toList());
    }
    public List<PointDto> getLoggedUserActivityInCourse(UUID courseId) {
        User loggedUser = userService.getLoggedUser();
        return getUsersActivityInCourse(loggedUser, courseId);
    }

    public List<PointDto> getUsersActivityInCourse(User user, UUID courseId){
        return pointRepository.findByStudentAndLesson_Course_Id(user, courseId).stream()
                .sorted(Comparator.comparing(p -> p.getLesson().getClassDate()))
                .map(a -> new PointDto(a)).collect(Collectors.toList());
    }

    public void addStudentActivity(UUID lessonId, Double value) {
        User loggedUser = userService.getLoggedUser();
        Optional<Point> p = pointRepository.findByLesson_IdAndStudent(lessonId,loggedUser);
        if(p.isEmpty()) {
            Point newPoint = new Point();
            newPoint.setActivityValue(value);
            newPoint.setStudent(loggedUser);
            Optional<Lesson> lesson = lessonRepository.findById(lessonId);
            if (lesson.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found");
            }
            newPoint.setLesson(lesson.get());
            pointRepository.save(newPoint);
        }else{
            Point point = p.get();
            point.setActivityValue(value);
            pointRepository.save(point);
        }
    }
}
