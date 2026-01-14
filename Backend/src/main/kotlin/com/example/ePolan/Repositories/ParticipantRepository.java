package com.example.ePolan.Repositories;

import com.example.ePolan.Model.Entities.Course;
import com.example.ePolan.Model.Entities.InvitationStatus;
import com.example.ePolan.Model.Entities.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ParticipantRepository extends JpaRepository<Participant, UUID> {

    Optional<Participant> findByStudent_IdAndCourse(String userId, Course course);

    Set<Participant> findByCourseAndInvitationStatus(Course course, InvitationStatus invitationStatus);

    Optional<Participant> findByStudent_EmailAndCourse(String email, Course course);
}
