package com.example.ePolan.Controllers;

import com.example.ePolan.Model.Dtos.DeclarationDto;
import com.example.ePolan.Model.Dtos.DeclarationShortDto;
import com.example.ePolan.Services.DeclarationService;
import com.example.ePolan.Services.ExerciseApplicationService;
import com.example.ePolan.Services.UserInfoService;
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
    private final UserInfoService userInfoService;
    private final ExerciseApplicationService exerciseApplicationService;

    @PostMapping("/{exerciseId}")
    public void declareExercise ( @PathVariable UUID exerciseId){
        String email = userInfoService.getLoggedUserInfo().getEmail();
        declarationService.declareExercise(email, exerciseId);
    }

    @GetMapping("")
    public ResponseEntity<List<DeclarationDto>> getStudentDeclarations (){
        String email = userInfoService.getLoggedUserInfo().getEmail();
        return ResponseEntity
                .ok()
                .cacheControl(CacheControl.maxAge(10, TimeUnit.MINUTES))
                .body(declarationService.getUsersDeclarations(email));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<DeclarationDto>> getStudentDeclarationsInCourse (@PathVariable UUID courseId){
        String email = userInfoService.getLoggedUserInfo().getEmail();
        return ResponseEntity
                .ok()
                .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).mustRevalidate())
                .body(declarationService.getUsersDeclarationsInCourse(email, courseId));
    }

    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<List<DeclarationDto>> getStudentDeclarationsForLesson (@PathVariable UUID lessonId){
        String email = userInfoService.getLoggedUserInfo().getEmail();
        return ResponseEntity
                .ok()
                .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).mustRevalidate())
                .body(declarationService.getDeclarationsForLesson(email, lessonId));
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
