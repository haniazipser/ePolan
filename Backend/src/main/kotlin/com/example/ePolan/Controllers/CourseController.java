package com.example.ePolan.Controllers;

import com.example.ePolan.Model.Dtos.CourseDto;
import com.example.ePolan.Model.requests.InviteStudentRequest;
import com.example.ePolan.Model.requests.JoinCourseRequest;
import com.example.ePolan.Model.requests.NewCourseRequest;
import com.example.ePolan.Model.Dtos.UserDto;
import com.example.ePolan.Services.CourseApplicationService;
import com.example.ePolan.Services.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/course")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final CourseApplicationService courseApplicationService;
    @PostMapping
    public CourseDto createGroup(@Valid @RequestBody NewCourseRequest newCourseRequest){
        return courseService.createCourse(newCourseRequest);
    }

    @GetMapping
    public ResponseEntity<List<CourseDto>> getStudentGroups(){
        return ResponseEntity
                .ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS))
                .body(courseService.getUsersGroups());
    }

    @GetMapping("/courses/archived")
    public ResponseEntity<List<CourseDto>> getStudentArchivedGroups(){
        return ResponseEntity
                .ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).mustRevalidate())
                .body(courseService.getUsersArchivedGroups());
    }

    @PutMapping("/{courseId}/restore")
    public void unarchiveCourse(@PathVariable UUID courseId){
        courseService.unarchiveCourse(courseId);
    }

    @GetMapping("/{courseId}/students")
    public ResponseEntity<List<UserDto>> getStudentsInGroup(@PathVariable UUID courseId){
        return ResponseEntity
                .ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS))
                .body(courseService.getStudentsInGroup(courseId));
    }

    @PostMapping("/{courseId}/invitations")
    public void inviteStudent(
            @PathVariable UUID courseId,
            @RequestBody InviteStudentRequest request) {

        courseApplicationService.addStudentToGroup(
                request.email(),
                courseId
        );
    }

    @PostMapping("/join")
    public void joinCourse(@RequestBody JoinCourseRequest request) {
        courseService.joinCourse(request.groupCode());
    }

    @DeleteMapping("/{courseId}/students/{userId}")
    public void deleteStudentFromGroup(@PathVariable UUID courseId, @PathVariable String userId){
        courseService.deleteStudentFromGroup(userId,courseId);
    }

    @DeleteMapping("/{courseId}")
    public void archiveCourse(@PathVariable UUID courseId){
        courseService.archiveCourse(courseId);
    }

}
