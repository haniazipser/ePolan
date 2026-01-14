package com.example.ePolan.Controllers;

import com.example.ePolan.Services.ExerciseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/exercise")
@RequiredArgsConstructor
public class ExerciseController {
    private final ExerciseService exerciseService;
   /* @PutMapping("")
    public void updateExercise (@RequestBody ExerciseDto exercise){
        exerciseService.updateExercsie(exercise);
    }

    @PostMapping("/{exerciseId}/{numberOfSubpoints}")
    public void addSubpointsToExercise (@PathVariable UUID exerciseId, @PathVariable Integer numberOfSubpoints){
        exerciseService.addSubpointsToExercise(exerciseId, numberOfSubpoints);
    }*/
}
