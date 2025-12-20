package com.example.ePolan.Services;

import com.example.ePolan.Model.Dtos.ExerciseWithPointsDto;
import com.example.ePolan.Model.Dtos.LessonDescriptionDto;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor

public class FileService {
    private final KeycloakClientService keycloakClientService;
    public String createPDFList(List<ExerciseWithPointsDto> exercises1, List<ExerciseWithPointsDto> exercises2, LessonDescriptionDto lesson) throws FileNotFoundException, DocumentException {
        Document document = new Document();
        LocalDate date = LocalDate.from(lesson.getClassDate().atZone(ZoneId.systemDefault()));
        String name = "List_" +lesson.getCourseName().replace(' ','_')+"_"+date.toString()+".pdf";
        PdfWriter.getInstance(document, new FileOutputStream(name));
        document.open();

        // Fonts
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.BLACK);
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 16, BaseColor.DARK_GRAY);
        Font exerciseFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);
        Font userFont = FontFactory.getFont(FontFactory.HELVETICA, 14, BaseColor.BLACK);

        Paragraph title = new Paragraph("List for " + lesson.getCourseName() + " " + date.toString(), titleFont);
        title.setSpacingAfter(5);
        document.add(title);

        LineSeparator underline = new LineSeparator();
        underline.setOffset(-3);
        document.add(underline);

        Paragraph subtitle = new Paragraph("By points", subtitleFont);
        subtitle.setSpacingAfter(20);
        document.add(subtitle);

// List of students per exercise
        int i = 1;
        for (ExerciseWithPointsDto e : exercises2) {
            StringBuilder line1 = new StringBuilder();
            StringBuilder line2 = new StringBuilder();
            String user = keycloakClientService.getUserByEmail(e.getApprovedStudent()).block();

            line1.append(i);
            line1.append(". ");
            line2.append(user).append(" - > ");
            line2.append("Ex. ").append(e.getExerciseNumber());

            if (e.getSubpoint() != null && !e.getSubpoint().isEmpty()) {
                line2.append(". ").append(e.getSubpoint()).append(") ");
            }

            Chunk chunk1 = new Chunk(line1.toString(), exerciseFont);
            Chunk chunk2 = new Chunk(line2.toString(),userFont);

            Paragraph paragraph = new Paragraph();
            paragraph.add(chunk1);
            paragraph.add(chunk2);
            paragraph.setSpacingAfter(10);

            document.add(paragraph);
            i++;
        }
        document.newPage();

        title.setSpacingAfter(5);
        document.add(title);

        underline.setOffset(-3);
        document.add(underline);

        subtitle = new Paragraph("By exercises", subtitleFont);
        subtitle.setSpacingAfter(20);
        document.add(subtitle);

        for (ExerciseWithPointsDto e : exercises1) {
            StringBuilder line1 = new StringBuilder();
            StringBuilder line2 = new StringBuilder();
            line1.append("Ex ").append(e.getExerciseNumber()).append(". ");

            if (e.getSubpoint() != null && !e.getSubpoint().isEmpty()) {
                line1.append(e.getSubpoint()).append(") ");
            }


            String user = keycloakClientService.getUserByEmail(e.getApprovedStudent()).block();

            line2.append(" - > ").append(user);

            Chunk chunk1 = new Chunk(line1.toString(), exerciseFont);
            Chunk chunk2 = new Chunk(line2.toString(),userFont);

            Paragraph paragraph = new Paragraph();
            paragraph.add(chunk1);
            paragraph.add(chunk2);

            paragraph.setSpacingAfter(10);

            document.add(paragraph);
        }

        document.close();
        return name;
    }
}