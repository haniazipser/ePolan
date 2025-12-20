package com.example.ePolan.Services.matching;

import com.example.ePolan.Model.Dtos.DeclarationShortDto;
import com.example.ePolan.Model.Entities.Exercise;
import com.example.ePolan.Model.Entities.User;
import com.example.ePolan.Repositories.ExerciseRepository;
import com.example.ePolan.Services.DeclarationService;
import com.example.ePolan.Services.ExerciseApplicationService;
import com.example.ePolan.Services.LessonService;
import com.example.ePolan.Services.UserService;
import com.example.ePolan.Services.matching.hungarianalgorithm.AssignmentAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.HashMap;



@Service
@RequiredArgsConstructor
public class MatchingService {
    private final DeclarationService declarationService;
    private final ExerciseRepository exerciseRepository;
    private final LessonService lessonService;
    private final ExerciseApplicationService exerciseApplicationService;
    private final UserService userService;
    private final MatchingStrategy matchingStrategy;

    @Scheduled(cron = "0 37 21 * * ?")
    @Transactional
    public void scheduleTask() {
        List<UUID> lessons = lessonService.getNextLessons();
        for (UUID l : lessons){
            matchingAlgorithm(l);
            exerciseApplicationService.exportListToPdf(l);
        }
    }

    public void matchingAlgorithm(UUID lessonId){
        List<DeclarationShortDto> declarations =
            declarationService.getAllDeclarationsForLesson(lessonId);

        // maps from id → index
        HashMap<String,Integer> studentMap = new HashMap<>();
        HashMap<UUID,Integer>   taskMap    = new HashMap<>();

        int nStudents = 0, nTasks = 0;

        // lists from index → id
        List<String> studentList = new ArrayList<>();
        List<UUID>   taskList    = new ArrayList<>();

        // build maps and lists
        for (DeclarationShortDto d : declarations) {
            String student = d.getStudent().getId();
            if (!studentMap.containsKey(student)) {
                studentMap.put(student, nStudents++);
                studentList.add(student);
            }

            UUID task = d.getExerciseId();
            if (!taskMap.containsKey(task)) {
                taskMap.put(task, nTasks++);
                taskList.add(task);
            }
        }

        // build cost matrix [tasks][students]
        double[][] cost = new double[nTasks][nStudents];

        for (DeclarationShortDto d : declarations) {
            int i = taskMap.get(d.getExerciseId());
            int j = studentMap.get(d.getStudent());
            cost[i][j] = d.getPointsInCourse();
        }

        // run the assignment 
        int[][] assignment = matchingStrategy.match(cost);

        // apply assignments
        for (int[] pair : assignment) {
            int studentIdx = pair[0];
            int taskIdx    = pair[1];

            String studentId = studentList.get(studentIdx);
            UUID   taskId    = taskList.get(taskIdx);

            Exercise ex = exerciseRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Exercise not found: " + taskId));

            User user = userService.getUserById(studentId);
            ex.setApprovedStudent(user);
            declarationService.rejectDeclarationsForExercise(taskId);
            exerciseRepository.save(ex);
        }
    }
}


