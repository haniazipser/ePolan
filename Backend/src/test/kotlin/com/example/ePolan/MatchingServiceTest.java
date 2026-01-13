package com.example.ePolan;

import com.example.ePolan.Model.Dtos.DeclarationShortDto;
import com.example.ePolan.Model.Dtos.UserDto;
import com.example.ePolan.Model.Entities.Exercise;
import com.example.ePolan.Model.Entities.User;
import com.example.ePolan.Repositories.ExerciseRepository;
import com.example.ePolan.Services.DeclarationService;
import com.example.ePolan.Services.ExerciseApplicationService;
import com.example.ePolan.Services.LessonService;
import com.example.ePolan.Services.UserService;
import com.example.ePolan.Services.matching.MatchingService;
import com.example.ePolan.Services.matching.MatchingStrategy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class MatchingServiceTest {

    @Mock
    DeclarationService declarationService;
    @Mock
    ExerciseRepository exerciseRepository;
    @Mock
    LessonService lessonService;
    @Mock
    ExerciseApplicationService exerciseApplicationService;
    @Mock
    UserService userService;
    @Mock
    MatchingStrategy matchingStrategy;

    @InjectMocks
    MatchingService matchingService;


    @Test
    void testMatchingAlgorithm_basicAssignment() {
        UUID lessonId = UUID.randomUUID();
        UUID ex1 = UUID.randomUUID();
        UUID ex2 = UUID.randomUUID();

        DeclarationShortDto d1 = new DeclarationShortDto(
                new UserDto("student1", "email1", "name1", "surname1"), ex1, 10.0);
        DeclarationShortDto d2 = new DeclarationShortDto(
                new UserDto("student2", "email2", "name2", "surname2"), ex2, 20.0);

        List<DeclarationShortDto> declarations = List.of(d1, d2);
        when(declarationService.getAllDeclarationsForLesson(lessonId))
                .thenReturn(declarations);


        int[][] assignment = {{0,0}, {1,1}};
        when(matchingStrategy.match(any(double[][].class)))
                .thenReturn(assignment);

        Exercise exObj1 = new Exercise();
        Exercise exObj2 = new Exercise();
        when(exerciseRepository.findById(ex1)).thenReturn(Optional.of(exObj1));
        when(exerciseRepository.findById(ex2)).thenReturn(Optional.of(exObj2));

        User user1 = new User();
        User user2 = new User();
        when(userService.getUserById("student1")).thenReturn(user1);
        when(userService.getUserById("student2")).thenReturn(user2);


        matchingService.matchingAlgorithm(lessonId);

        assertEquals(user1, exObj1.getApprovedStudent());
        assertEquals(user2, exObj2.getApprovedStudent());

        verify(declarationService).rejectDeclarationsForExercise(ex1);
        verify(declarationService).rejectDeclarationsForExercise(ex2);


        verify(exerciseRepository).save(exObj1);
        verify(exerciseRepository).save(exObj2);
    }

    @Test
    void testMatchingAlgorithm_exerciseNotFound_throwsException() {
        UUID lessonId = UUID.randomUUID();
        UUID ex1 = UUID.randomUUID();

        DeclarationShortDto d1 = new DeclarationShortDto(
                new UserDto("student1", "email1", "name1", "surname1"), ex1, 10.0);
        List<DeclarationShortDto> declarations = List.of(d1);
        when(declarationService.getAllDeclarationsForLesson(lessonId))
                .thenReturn(declarations);


        when(matchingStrategy.match(any(double[][].class)))
                .thenReturn(new int[][]{{0,0}});


        when(exerciseRepository.findById(ex1)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> matchingService.matchingAlgorithm(lessonId));


        assertTrue(exception.getReason().contains(ex1.toString()));
    }

    @Test
    void testScheduleTask_callsMatchingAndExport() {
        UUID lessonId = UUID.randomUUID();

        when(lessonService.getNextLessons()).thenReturn(List.of(lessonId));
        doNothing().when(exerciseApplicationService).exportListToPdf(lessonId);
        when(declarationService.getAllDeclarationsForLesson(lessonId)).thenReturn(List.of());
        when(matchingStrategy.match(any(double[][].class))).thenReturn(new int[][]{});

        matchingService.scheduleTask();

        verify(lessonService).getNextLessons();
        verify(exerciseApplicationService).exportListToPdf(lessonId);
        verify(declarationService).getAllDeclarationsForLesson(lessonId);
    }


}
