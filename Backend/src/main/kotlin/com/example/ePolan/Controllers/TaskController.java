package com.example.ePolan.Controllers;

import com.example.ePolan.Services.CourseApplicationService;
import com.example.ePolan.Services.FileService;
import com.example.ePolan.Services.UserInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final UserInfoService userInfoService;
    private final CourseApplicationService courseApplicationService;
    private final FileService fileService;

    /*@GetMapping("")
    public Set<TaskDto> getStudentsTasks(){
        System.out.println("w kontrolerze");
        String email = userInfoService.getLoggedUserInfo().getEmail();
        return courseApplicationService.getStudentsTasks(email);
    }
*/

}
