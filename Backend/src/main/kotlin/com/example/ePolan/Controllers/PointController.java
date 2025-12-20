package com.example.ePolan.Controllers;

import com.example.ePolan.Services.PointService;
import com.example.ePolan.Model.Dtos.PointDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
@RequestMapping("/points")
public class PointController {
    private final PointService pointService;
    @GetMapping("")
    public ResponseEntity<List<PointDto>> getUsersActivity(){
        return ResponseEntity
                .ok()
                .cacheControl(CacheControl.maxAge(10, TimeUnit.MINUTES))
                .body(pointService.getUsersActivity());
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<List<PointDto>> getUsersActivityInCourse(@PathVariable UUID courseId){
        return ResponseEntity
                .ok()
                .cacheControl(CacheControl.maxAge(10, TimeUnit.MINUTES))
                .body(pointService.getLoggedUserActivityInCourse(courseId));
    }


    @PostMapping("/{lessonId}/{value}")
    public void addStudentActivity( @PathVariable UUID lessonId, @PathVariable Double value){
        pointService.addStudentActivity(lessonId,value);
    }



}
