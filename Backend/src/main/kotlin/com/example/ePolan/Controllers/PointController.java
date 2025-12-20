package com.example.ePolan.Controllers;

import com.example.ePolan.Services.PointService;
import com.example.ePolan.Model.Dtos.PointDto;
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
@RequestMapping("/points")
public class PointController {
    private final PointService pointService;
    private final UserInfoService userInfoService;
    @GetMapping("")
    public ResponseEntity<List<PointDto>> getUsersActivity(){
        String email = userInfoService.getLoggedUserInfo().getEmail();
        return ResponseEntity
                .ok()
                .cacheControl(CacheControl.maxAge(10, TimeUnit.MINUTES))
                .body(pointService.getUsersActivity(email));
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<List<PointDto>> getUsersActivityInCourse(@PathVariable UUID courseId){
        String email = userInfoService.getLoggedUserInfo().getEmail();
        return ResponseEntity
                .ok()
                .cacheControl(CacheControl.maxAge(10, TimeUnit.MINUTES))
                .body(pointService.getUsersActivityInCourse(email,courseId));
    }


    @PostMapping("/{lessonId}/{value}")//wysylasz mi z formularza obiekt z polami jak ma activity dto
    public void addStudentActivity( @PathVariable UUID lessonId, @PathVariable Double value){
        String email = userInfoService.getLoggedUserInfo().getEmail();
        pointService.addStudentActivity(email,lessonId,value);
    }



}
