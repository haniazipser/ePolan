package com.example.ePolan.Controllers;

import com.example.ePolan.Model.Dtos.ExerciseDto;
import com.example.ePolan.Model.Dtos.LessonDto;
import com.example.ePolan.Services.LessonService;
import com.example.ePolan.Services.ExerciseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lesson")
public class LessonController {
    private final ExerciseService exerciseService;
    private final LessonService lessonService;
    @GetMapping("/{lessonId}/exercises")
    public ResponseEntity<List<ExerciseDto>> getExercisesForLesson(@PathVariable UUID lessonId){
        return ResponseEntity
                .ok()
                .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS))
                .body(exerciseService.getExercisesForLesson(lessonId));
    }

    @DeleteMapping("/{lessonId}")
    public void deleteLesson(@PathVariable UUID lessonId){
        lessonService.deleteLesson(lessonId);
    }

    @GetMapping("/{courseId}/lessons")
    public ResponseEntity<List<LessonDto>> getLessonsForCourse (@PathVariable UUID courseId){

        return ResponseEntity
                .ok()
                .cacheControl(CacheControl.maxAge(10, TimeUnit.DAYS))
                .body(lessonService.getLessonsForCourse(courseId));
    }

   @PutMapping("/exercises")
    public void updateExercisesForLesson(@Valid @RequestBody LessonDto lesson){
        lessonService.updateExercisesForLesson(lesson);
    }

    @PutMapping("{courseId}/{date}/addLesson")
    public LessonDto addLesson(@PathVariable UUID courseId, @PathVariable Instant date){
        return lessonService.addNewLesson(courseId,date);
    }
}
