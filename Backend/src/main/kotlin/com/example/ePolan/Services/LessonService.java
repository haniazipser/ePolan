package com.example.ePolan.Services;

import com.example.ePolan.Model.Dtos.LessonDescriptionDto;
import com.example.ePolan.Model.Dtos.LessonDto;
import com.example.ePolan.Model.Dtos.ExerciseDto;
import com.example.ePolan.Model.Entities.Course;
import com.example.ePolan.Model.Entities.Lesson;
import com.example.ePolan.Model.Entities.Exercise;
import com.example.ePolan.Repositories.CourseRepository;
import com.example.ePolan.Repositories.LessonRepository;
import com.example.ePolan.Repositories.ExerciseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LessonService {
    private final LessonRepository lessonRepository;
    private final ExerciseRepository exerciseRepository;
    private final CourseRepository courseRepository;

    public LessonDto addNewLesson(UUID courseId, Instant date){
        Optional<Course> c = courseRepository.findById(courseId);
        if (c.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found");
        }
        Lesson lesson = new Lesson();
        Course course = c.get();
        lesson.setCourse(course);
        lesson.setClassDate(date);
        lesson = lessonRepository.save(lesson);
        return new LessonDto(lesson);
    }

    public LessonDto getNextLesson(UUID courseId){
        Optional<Lesson> lesson = lessonRepository.findTopByCourse_IdAndClassDateAfterOrderByClassDateAsc(courseId, Instant.now());
        if (lesson.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NO_CONTENT, "There are no future lessons added for this group");
        }
        return new LessonDto(lesson.get());
    }

    public void deleteLesson(UUID lessonId) {
        Optional<Lesson> lesson = lessonRepository.findById(lessonId);
        if (lesson.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This lesson does not exist");
        } else if (lesson.get().getClassDate().isBefore(Instant.now())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can not delete lesson from the past");
        }
        lessonRepository.delete(lesson.get());
    }

    public List<LessonDto> getLessonsForCourse(UUID courseId, String email) {
        Optional<Course> course = courseRepository.findById(courseId);
        if (course.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found");
        } else if (!course.get().isStudentAMemeber(email)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this course");
        }

        return lessonRepository.findByCourse(course.get()).stream()
                .sorted(Comparator.comparing(Lesson::getClassDate))
                .map(l -> new LessonDto(l)).collect(Collectors.toList());
    }

    public void updateExercisesForLesson(LessonDto lesson) {
        Optional<Lesson> l = lessonRepository.findById(lesson.getId());
        if (l.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found");
        }
        Set<Exercise> exercises = exerciseRepository.findByLesson(l.get());
        for (Exercise e : exercises){
            if (!e.getDeclarations().isEmpty()){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There are already declarations in this class");
            }
        }

        for (ExerciseDto e : lesson.getExercises()) {
            Exercise exercise = new Exercise();
            exercise.setLesson(l.get());
            exercise.setExerciseNumber( e.getExerciseNumber());
            exercise.setSubpoint(e.getSubpoint());
            exerciseRepository.save(exercise);
        }
    }

    public LessonDescriptionDto getLessonInfo(UUID lessonId){
        return new LessonDescriptionDto(lessonRepository.findById(lessonId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found")));
    }

    public List<UUID> getNextLessons() {
        ZoneId zone = ZoneId.systemDefault();

        LocalDate tomorrow = LocalDate.now(zone).plusDays(1);
        Instant startOfDay = tomorrow.atStartOfDay(zone).toInstant();
        Instant endOfDay = tomorrow.plusDays(1).atStartOfDay(zone).toInstant();

        return lessonRepository.findLessonIdsByClassDateBetween(startOfDay, endOfDay);
    }
/*
    public void updateExercisesForLesson(LessonDto lessonDto) {
        Optional<Lesson> l = lessonRepository.findById(lessonDto.getId());
        if (l.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found");
        }
        Lesson lesson = l.get();
        Set<Exercise> exercises;
        for (ExerciseDto e : lessonDto.getExercises()){
            Exercise exercise = new Exercise();
            exercise.setLesson(lesson);
            exercise.setExerciseNumber(e.getExerciseNumber());
            exercise.setSubpoint(e.getSubpoint());
            exerciseRepository.save()
        }

    }*/
}
