package com.example.ePolan;

import com.example.ePolan.Services.filegenerator.PdfParagraphBuilder;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.draw.LineSeparator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PdfParagraphBuilderTest {

    @Test
    void testBuildParagraphWithAllFeatures() {
        Font font = new Font();
        String text = "Hello world";

        PdfParagraphBuilder builder = PdfParagraphBuilder.create()
                .addText(text, font)
                .addText("Another line")
                .withSpacingBefore(10f)
                .withSpacingAfter(5f)
                .centered()
                .withIndentationLeft(15f)
                .withIndentationRight(20f)
                .withFirstLineIndent(5f)
                .withLeading(12f)
                .withLineSeparator(-2f);


        Paragraph paragraph = builder.build();
        assertNotNull(paragraph);


        assertEquals(10f, paragraph.getSpacingBefore(), 0.001);
        assertEquals(5f, paragraph.getSpacingAfter(), 0.001);
        assertEquals(Element.ALIGN_CENTER, paragraph.getAlignment());
        assertEquals(15f, paragraph.getIndentationLeft(), 0.001);
        assertEquals(20f, paragraph.getIndentationRight(), 0.001);
        assertEquals(5f, paragraph.getFirstLineIndent(), 0.001);
        assertEquals(12f, paragraph.getLeading(), 0.001);

    }

    @Test
    void testAddNullOrEmptyTextDoesNotAddChunk() {
        PdfParagraphBuilder builder = PdfParagraphBuilder.create()
                .addText(null)
                .addText("");

        Paragraph paragraph = builder.build();
        assertEquals(0, paragraph.getChunks().size());
    }

    @Test
    void testAddChunkAndAddTexts() {
        Font font = new Font();
        PdfParagraphBuilder builder = PdfParagraphBuilder.create();

        builder.addChunk(new com.itextpdf.text.Chunk("Chunk test"));

        String[] texts = {"One", "Two"};
        Font[] fonts = {font, font};
        builder.addTexts(texts, fonts);

        Paragraph paragraph = builder.build();
        assertNotNull(paragraph);


        String paragraphText = paragraph.getContent();
        assertTrue(paragraphText.contains("Chunk test"));
        assertTrue(paragraphText.contains("One"));
        assertTrue(paragraphText.contains("Two"));
    }

    @Test
    void testBuildWithElements_withoutLineSeparator() {
        PdfParagraphBuilder builder = PdfParagraphBuilder.create()
                .addText("Hello world");

        Element[] elements = builder.buildWithElements();

        assertNotNull(elements);
        assertEquals(1, elements.length);
        assertTrue(elements[0] instanceof Paragraph);

        Paragraph paragraph = (Paragraph) elements[0];
        assertTrue(paragraph.getContent().contains("Hello world"));
    }

    @Test
    void testBuildWithElements_withLineSeparator() {
        PdfParagraphBuilder builder = PdfParagraphBuilder.create()
                .addText("Hello world")
                .withLineSeparator(-5f);

        Element[] elements = builder.buildWithElements();

        assertNotNull(elements);
        assertEquals(2, elements.length);

        // Pierwszy element to Paragraph
        assertTrue(elements[0] instanceof Paragraph);
        assertTrue(((Paragraph) elements[0]).getContent().contains("Hello world"));

        // Drugi element to LineSeparator
        assertTrue(elements[1] instanceof LineSeparator);
        assertEquals(-5f, ((LineSeparator) elements[1]).getOffset(), 0.001);
    }

}
