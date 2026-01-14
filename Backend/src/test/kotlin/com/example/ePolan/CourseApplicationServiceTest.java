package com.example.ePolan;

import com.example.ePolan.Model.Dtos.CourseDto;
import com.example.ePolan.Services.CourseApplicationService;
import com.example.ePolan.Services.CourseService;
import com.example.ePolan.Services.messagesender.EmailMessageSender;
import com.example.ePolan.Services.messagesender.GroupInvitationMessage;
import com.example.ePolan.Services.messagesender.Message;
import com.example.ePolan.Services.messagesender.MessageFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseApplicationServiceTest {

    @Mock
    CourseService courseService;
    @Mock
    EmailMessageSender emailSender;

    private MessageFactory messageFactory;
    private CourseApplicationService service;

    @BeforeEach
    void setUp() {

        messageFactory = new MessageFactory(emailSender);

        service = new CourseApplicationService(courseService, messageFactory);
    }



    @Test
    void testAddStudentToGroup_sendsInvitation() throws Exception {
        String email = "student@example.com";
        UUID courseId = UUID.randomUUID();

        CourseDto fakeCourse = new CourseDto();
        when(courseService.getGroupInfo(courseId)).thenReturn(fakeCourse);

        service.addStudentToGroup(email, courseId);


        verify(courseService).addStudentToGroup(email, courseId);
        verify(courseService).getGroupInfo(courseId);

        verify(emailSender).send(
                anyString(),
                anyString(),
                anyString(),
                any()
        );
    }

    @Test
    void testAddStudentToGroup_logsErrorOnException() throws Exception {
        String email = "student@example.com";
        UUID courseId = UUID.randomUUID();

        CourseDto fakeCourse = new CourseDto();
        when(courseService.getGroupInfo(courseId)).thenReturn(fakeCourse);


        doThrow(new RuntimeException("SMTP error"))
                .when(emailSender)
                .send(anyString(), anyString(), anyString(), any());


        assertDoesNotThrow(() -> service.addStudentToGroup(email, courseId));

        verify(emailSender).send(anyString(), anyString(), anyString(), any());
    }
}
