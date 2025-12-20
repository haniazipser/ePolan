package com.example.ePolan.Model.Dtos;

import com.example.ePolan.Model.Entities.Point;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter @Setter
public class PointDto {
        private UUID id;
        private UserDto student;
        private LessonDto lesson;
        private Double activityValue;
        public PointDto(Point point){
            this.id= point.getId();
            this.student = new UserDto(point.getStudent());
            this.lesson = new LessonDto(point.getLesson());
            this.activityValue = point.getActivityValue();
        }
        public PointDto(){}
}
