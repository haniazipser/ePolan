package com.example.ePolan.Services;

import com.example.ePolan.Model.Entities.DeclarationStatus;
import com.example.ePolan.Model.Dtos.DeclarationDto;
import com.example.ePolan.Model.Dtos.DeclarationShortDto;
import com.example.ePolan.Model.Dtos.PointDto;
import com.example.ePolan.Model.Entities.Exercise;
import com.example.ePolan.Model.Entities.ExerciseDeclaration;
import com.example.ePolan.Repositories.DeclarationRepository;
import com.example.ePolan.Repositories.ExerciseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DeclarationService {
    private  final DeclarationRepository declarationRepository;
    private final ExerciseRepository exerciseRepository;
    private final PointService pointService;//CZY TO TU MOZE BYC??
    public void declareExercise(String email, UUID exerciseId) {
        ExerciseDeclaration declaration = new ExerciseDeclaration();
        Optional<Exercise> exercise = exerciseRepository.findById(exerciseId);
        if (exercise.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Exercise not found");
        }else if(exercise.get().getLesson().getClassDate().isBefore(Instant.now())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can not declare exercises frm past lessons");
        }else if (!exercise.get().getLesson().getCourse().isStudentAMemeber(email)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tou are not a member of this course");
        }else if (exercise.get().getLesson().getHoursToLesson() < 12){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can no longer declare exercises");
        }

        declaration.setExercise(exercise.get());
        declaration.setDeclarationDate(Instant.now());
        declaration.setStudent(email);
        declaration.setDeclarationStatus(DeclarationStatus.WAITING);
        declarationRepository.save(declaration);
    }

    public List<DeclarationDto> getUsersDeclarations(String email) {
        return declarationRepository.findByStudent(email).stream().map(d -> new DeclarationDto(d)).sorted(Comparator.comparing(DeclarationDto::getId)).collect(Collectors.toList());
    }

    public void runMatchingAlgorithm(){
        //nasz algorytm jak dopasuje exercise, student musi zrobic:
       /* Exercise exercise = exerciseRepository.findById(exerciseId);
        exercise.setApprovedStudent(email);
        Set<ExerciseDeclaration> rejectedDeclarations = declarationRepository.findByExercise_Id(exerciseId);
        for (ExerciseDeclaration r : rejectedDeclarations){
            r.setDeclarationStatus(DeclarationStatus.REJECTED);
            declarationRepository.save(r);
        }*/

    }

    public List<DeclarationDto> getDeclarationsForLesson(String email, UUID id) {
        return declarationRepository.findByStudentAndExercise_Lesson_Id(email,id)
                .stream().map(d -> new DeclarationDto(d)).sorted(Comparator.comparing(DeclarationDto::getId)).collect(Collectors.toList());
    }

    public List<DeclarationShortDto> getAllDeclarationsForLesson(UUID lessonId) {
        Set<ExerciseDeclaration> declarations = declarationRepository.findByExercise_Lesson_Id(lessonId);
        List<DeclarationShortDto> shortDeclarations = new ArrayList<>();
        for (ExerciseDeclaration declaration : declarations){
            List<PointDto>activity = pointService.getUsersActivityInCourse(declaration.getStudent(), declaration.getExercise().getLesson().getCourse().getId());
            Double sum = activity.stream().mapToDouble(PointDto::getActivityValue).sum();
            shortDeclarations.add(new DeclarationShortDto(declaration,sum));
        }
        return shortDeclarations.stream().sorted(Comparator.comparing(DeclarationShortDto::getStudent)).collect(Collectors.toList());
    }

    public void rejectDeclarationsForExercise(UUID exerciseId) {
        Set<ExerciseDeclaration> declarations = declarationRepository.findByExercise_Id(exerciseId);
        for (ExerciseDeclaration declaration : declarations){
           declaration.setDeclarationStatus(DeclarationStatus.REJECTED);
           declarationRepository.save(declaration);
        }
    }

    public Integer getDeclarationsForSessionCount(String email, UUID id) {
        return declarationRepository.countByStudentAndExercise_Lesson_Id(email,id);
    }

    public List<DeclarationDto> getUsersDeclarationsInCourse(String email, UUID courseId) {
        return declarationRepository.findByStudentAndExercise_Lesson_Course_Id(email,courseId)
                .stream().map(d -> new DeclarationDto(d)).sorted(Comparator.comparing(DeclarationDto::getId)).collect(Collectors.toList());
    }

    public void deleteDeclaration(UUID declarationId) {
        Optional<ExerciseDeclaration> declaration = declarationRepository.findById(declarationId);
        if (declaration.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Declaration not found");
        } else if (declaration.get().getDeclarationStatus() == DeclarationStatus.APPROVED){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can not modify this declaration");
        }
        declarationRepository.delete(declaration.get());
    }

}
