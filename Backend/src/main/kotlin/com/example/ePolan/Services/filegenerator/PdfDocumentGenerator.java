package com.example.ePolan.Services.filegenerator;

import com.example.ePolan.Model.Dtos.ExerciseWithPointsDto;
import com.example.ePolan.Model.Dtos.LessonDescriptionDto;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
@Component
public class PdfDocumentGenerator extends BaseDocumentGenerator {
    private Document document;
    private Font titleFont;
    private Font subtitleFont;
    private Font exerciseFont;
    private Font userFont;
    @Override
    protected void initializeDocument(String filename) throws FileNotFoundException, DocumentException {
        document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(filename));
        document.open();

        titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.BLACK);
        subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 16, BaseColor.DARK_GRAY);
        exerciseFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);
        userFont = FontFactory.getFont(FontFactory.HELVETICA, 14, BaseColor.BLACK);
    }

    @Override
    protected void addHeader(LessonDescriptionDto lesson) throws DocumentException {
        LocalDate date = LocalDate.from(lesson.getClassDate().atZone(ZoneId.systemDefault()));
        PdfParagraphBuilder
                .create()
                .addText("List for " + lesson.getCourseName() + " " + date.toString(), titleFont)
                .withSpacingAfter(5)
                .withLineSeparator(-3)
                .addTo(document);
    }

    @Override
    protected void addByPointsSection(List<ExerciseWithPointsDto> exercises) throws DocumentException {
        int i = 1;
        for (ExerciseWithPointsDto e : exercises) {

            PdfParagraphBuilder builder = PdfParagraphBuilder.create()
                    .addText(i + ". ", exerciseFont)
                    .addText(e.getApprovedStudent().getName() + " " + e.getApprovedStudent().getSurname() + " -> Ex. " + e.getExerciseNumber(), userFont);

            if (e.getSubpoint() != null && !e.getSubpoint().isEmpty()) {
                builder.addText(". " + e.getSubpoint() + ") ", userFont);
            }

            builder.withSpacingAfter(10).addTo(document);
            i++;
        }
    }

    @Override
    protected void addByExercisesSection(List<ExerciseWithPointsDto> exercises) throws DocumentException {
        PdfParagraphBuilder
                .create()
                .addText("By exercises", subtitleFont)
                .withSpacingAfter(5)
                .addTo(document);

        for (ExerciseWithPointsDto e : exercises) {
            PdfParagraphBuilder builder = PdfParagraphBuilder.create()
                    .addText("Ex " + e.getExerciseNumber() + ". ", exerciseFont);

            if (e.getSubpoint() != null && !e.getSubpoint().isEmpty()) {
                builder.addText(e.getSubpoint() + ") ", exerciseFont);
            }

            builder.addText(" -> " +e.getApprovedStudent().getName() + " " + e.getApprovedStudent().getSurname(), userFont)
                    .withSpacingAfter(10)
                    .addTo(document);
        }

    }

    @Override
    protected void startNewPage() {
        document.newPage();
    }

    @Override
    protected void closeDocument(){
        document.close();
    }

    @Override
    public String getFileExtension() {
        return "pdf";
    }
}
