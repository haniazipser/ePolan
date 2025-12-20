package com.example.ePolan.Services;

import com.example.ePolan.Model.Dtos.CourseDto;
import com.example.ePolan.Model.Dtos.NewCourseDto;
import com.example.ePolan.Model.Dtos.UserDto;

import com.example.ePolan.Model.Entities.Course;
import com.example.ePolan.Model.Entities.InvitationStatus;
import com.example.ePolan.Model.Entities.Lesson;
import com.example.ePolan.Model.Entities.LessonTime;
import com.example.ePolan.Model.Entities.Participant;
import com.example.ePolan.Model.Entities.User;
import com.example.ePolan.Repositories.CourseRepository;

import com.example.ePolan.Repositories.LessonRepository;
import com.example.ePolan.Repositories.ParticipantRepository;
import com.example.ePolan.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseService {
    private final CourseRepository courseRepository;
    private final ParticipantRepository participantRepository;
    private final LessonRepository lessonRepository;
    private final UserService userService;
    Logger logger = LoggerFactory.getLogger(CourseService.class);
    public List<CourseDto> getUsersGroups (){
        User loggedUser = userService.getLoggedUser();

        return courseRepository.findDistinctByStudents_IdAndStudents_InvitationStatus(loggedUser.getId(), InvitationStatus.ACCEPTED)
              .stream().map(g -> new CourseDto(g)).sorted(Comparator.comparing(CourseDto::getId)).collect(Collectors.toList());
    }

    public CourseDto createCourse(NewCourseDto courseDto) {
        User loggedUser = userService.getLoggedUser();

        Course course = new Course();
        course.setCreator(loggedUser);
        course.setLessonTimes(courseDto.getLessonTimes());
        course.setName(courseDto.getName());
        course.setInstructor(courseDto.getInstructor());
        course.setStartDate(courseDto.getStartDate());
        course.setEndDate(courseDto.getEndDate());
        course.setFrequency(courseDto.getFrequency());
        course.setCourseCode(UUID.randomUUID().toString());

        course = courseRepository.save(course);

        Set<Lesson> lessons = new HashSet<>();

        Instant endDate = courseDto.getEndDate();
        for (LessonTime lessonTime : course.getLessonTimes()) {
            Instant next = course.getFirstLesson(lessonTime);
            System.out.println("Dodaję lekcję: " + next);
            while (next.isBefore(endDate)){
                Lesson lesson = new Lesson();
                lesson.setClassDate(next);
                lesson.setCourse(course);
                lesson.setLessonExercises(Collections.emptySet());
                lessonRepository.save(lesson);
                lessons.add(lesson);
                next  = next.plus(7 * course.getFrequency(), ChronoUnit.DAYS);
            }
        }
        course.setLessons(lessons);

        Participant participant = new Participant();
        participant.setCourse(course);
        participant.setStudent(loggedUser);
        participant.setInvitationStatus(InvitationStatus.ACCEPTED);
        participantRepository.save(participant);
        return new CourseDto(course);
    }

    public void addStudentToGroup(String userId, UUID courseId) {
        Optional<Course> course = courseRepository.findById(courseId);
        User loggedUser = userService.getLoggedUser();
        if (course.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found");
        }else if (!course.get().isStudentACreator(loggedUser)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You are not authorized to add students to this group");
        }

        if (!participantRepository.findByUser_IdAndCourse(userId, course.get()).isEmpty()){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"This user is already in this course");
        }

        User student = userService.getUserById(userId);

        Participant participant = new Participant();
        participant.setStudent(student);
        participant.setCourse(course.get());
        participant.setInvitationStatus(InvitationStatus.WAITING);

        participantRepository.save(participant);
    }

    public void deleteStudentFromGroup(String userId, UUID groupId) {
        Optional<Course> course = courseRepository.findById(groupId);
        User loggedUser = userService.getLoggedUser();
        if (course.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found");
        }else if (course.get().isStudentACreator(loggedUser)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You are not authorized to delete students to this group");
        }

        Optional<Participant> participant = participantRepository.findByUser_IdAndCourse(userId, course.get());
        if (participant.isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"This student is not a member");
        }

        participantRepository.delete(participant.get());
    }

    public List<UserDto> getStudentsInGroup(UUID groupId) {
        Optional<Course> course = courseRepository.findById(groupId);
        User loggedUser = userService.getLoggedUser();

        if (course.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found");
        }else if (!course.get().isStudentAMemeber(loggedUser)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You are not a member of this group");
        }

        return participantRepository
                .findByCourseAndInvitationStatus(course.get(), InvitationStatus.ACCEPTED)
                .stream()
                .map(p -> new UserDto(p.getStudent())).collect(Collectors.toList());
    }

    public CourseDto getGroupInfo(UUID groupId){
        Course course = courseRepository.findById(groupId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
        return new CourseDto(course);
    }

    public void archiveCourse(UUID courseId) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
        User loggedUser = userService.getLoggedUser();
       for (Participant p : course.getStudents()) {
           if (p.getStudent().getId().equals(loggedUser.getId())) {
                p.setInvitationStatus(InvitationStatus.ARCHIVED);
                participantRepository.save(p);
                break;
           }
       }
    }

    public void joinCourse(String groupCode) {
        Optional<Course> course = courseRepository.findByCourseCode(groupCode);
        User loggedUser = userService.getLoggedUser();

        if (course.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found");
        }
        Participant participant;
        Optional<Participant> p = participantRepository.findByUser_IdAndCourse(loggedUser.getId(),course.get());
        if (p.isEmpty()){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You were not invited to this course");
        }
        participant = p.get();
        participant.setInvitationStatus(InvitationStatus.ACCEPTED);
        participantRepository.save(participant);

    }

    public List<CourseDto> getUsersArchivedGroups() {
        User loggedUser = userService.getLoggedUser();

        return courseRepository.findDistinctByStudents_IdAndStudents_InvitationStatus(loggedUser.getId(), InvitationStatus.ARCHIVED)
                .stream().map(g -> new CourseDto(g)).sorted(Comparator.comparing(CourseDto::getId)).collect(Collectors.toList());
    }

    public void unarchiveCourse(UUID courseId) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
        User loggedUser = userService.getLoggedUser();
        for (Participant p : course.getStudents()) {
            if (p.getStudent().getId().equals(loggedUser.getId())) {
                p.setInvitationStatus(InvitationStatus.ACCEPTED);
                participantRepository.save(p);
                break;
            }
        }
    }
}
