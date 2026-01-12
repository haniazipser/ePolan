package com.example.ePolan.Services;

import com.example.ePolan.Model.Dtos.ExerciseDto;
import com.example.ePolan.Model.Dtos.ExerciseWithPointsDto;
import com.example.ePolan.Model.Dtos.PointDto;
import com.example.ePolan.Model.Entities.Exercise;
import com.example.ePolan.Model.Entities.User;
import com.example.ePolan.Repositories.ExerciseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ExerciseService {
    private final ExerciseRepository exerciseRepository;
    private final PointService pointService;
    public List<ExerciseDto> getExercisesForLesson(UUID lessonId) {
        return exerciseRepository.findByLesson_Id(lessonId).stream()
                .map(ExerciseDto::new)
                .sorted(Comparator.comparing(ExerciseDto::getExerciseNumber).thenComparing(
                                Comparator.comparing(ExerciseDto::getSubpoint, Comparator.nullsFirst(Comparator.naturalOrder()))
                        ))
                .collect(Collectors.toList());
    }


    public Set<ExerciseWithPointsDto> getList(UUID lessonId) {
        Set<Exercise> exercises = exerciseRepository.findByLesson_Id(lessonId);

        if (exercises.isEmpty()) {
            return new HashSet<>();
        }

        UUID courseId = exercises.iterator().next().getLesson().getCourse().getId();

        Set<User> students = exercises.stream()
                .map(Exercise::getApprovedStudent)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<User, Double> pointsByStudent = students.stream()
                .collect(Collectors.toMap(
                        student-> student,
                        student -> pointService.getUsersActivityInCourse(student, courseId).stream()
                                .mapToDouble(PointDto::getActivityValue)
                                .sum()
                ));

        return exercises.stream()
                .filter(exercise -> exercise.getApprovedStudent() != null)
                .map(exercise -> new ExerciseWithPointsDto(
                        exercise,
                        pointsByStudent.getOrDefault(exercise.getApprovedStudent(), 0.0)
                ))
                .collect(Collectors.toSet());
    }

    public ExerciseDto getById(UUID id){
        Exercise ex = exerciseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Exercise not found: " + id));

        return new ExerciseDto(ex);
    }
}
