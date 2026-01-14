package com.example.ePolan.Controllers;

import com.example.ePolan.Model.Dtos.DeclarationDto;
import com.example.ePolan.Model.Dtos.DeclarationShortDto;
import com.example.ePolan.Services.DeclarationService;
import com.example.ePolan.Services.ExerciseApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
@RequestMapping("/declaration")
public class DeclarationController {
    private final DeclarationService declarationService;
    private final ExerciseApplicationService exerciseApplicationService;

    @PostMapping("/{exerciseId}")
    public void declareExercise ( @PathVariable UUID exerciseId){
        declarationService.declareExercise(exerciseId);
    }

    @GetMapping("")
    public ResponseEntity<List<DeclarationDto>> getStudentDeclarations (){
        return ResponseEntity
                .ok()
                .cacheControl(CacheControl.maxAge(10, TimeUnit.MINUTES))
                .body(declarationService.getUsersDeclarations());
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<DeclarationDto>> getStudentDeclarationsInCourse (@PathVariable UUID courseId){
        return ResponseEntity
                .ok()
                .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).mustRevalidate())
                .body(declarationService.getUsersDeclarationsInCourse(courseId));
    }

    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<List<DeclarationDto>> getStudentDeclarationsForLesson (@PathVariable UUID lessonId){
        return ResponseEntity
                .ok()
                .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).mustRevalidate())
                .body(declarationService.getDeclarationsForLesson(lessonId));
    }

    @GetMapping("/test/{lessonId}")
    public List<DeclarationShortDto> testAllDeclarationsForLesson(@PathVariable UUID lessonId){
        return declarationService.getAllDeclarationsForLesson(lessonId);
    }

    @DeleteMapping("/{declarationId}")
    public void deleteDeclaration (@PathVariable UUID declarationId){
        declarationService.deleteDeclaration (declarationId);
    }

    @PostMapping("/{lessonId}/send")
    public void sendList(@PathVariable UUID lessonId){
        exerciseApplicationService.exportListToPdf(lessonId);
    }
}
