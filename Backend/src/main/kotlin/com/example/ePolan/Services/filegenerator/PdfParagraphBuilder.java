package com.example.ePolan.Services.filegenerator;


import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.draw.LineSeparator;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for creating PDF paragraphs
 */
public class PdfParagraphBuilder {
    private final List<Chunk> chunks = new ArrayList<>();
    private float spacingBefore = 0f;
    private float spacingAfter = 0f;
    private int alignment = Element.ALIGN_LEFT;
    private float indentationLeft = 0f;
    private float indentationRight = 0f;
    private float firstLineIndent = 0f;
    private float leading = 0f;
    private boolean addLineSeparator = false;
    private float lineSeparatorOffset = 0f;

    public static PdfParagraphBuilder create() {
        return new PdfParagraphBuilder();
    }

    public PdfParagraphBuilder addText(String text, Font font) {
        if (text != null && !text.isEmpty()) {
            chunks.add(new Chunk(text, font));
        }
        return this;
    }

    public PdfParagraphBuilder addText(String text) {
        if (text != null && !text.isEmpty()) {
            chunks.add(new Chunk(text));
        }
        return this;
    }

    public PdfParagraphBuilder addChunk(Chunk chunk) {
        if (chunk != null) {
            chunks.add(chunk);
        }
        return this;
    }

    public PdfParagraphBuilder addTexts(String[] texts, Font[] fonts) {
        if (texts != null && fonts != null && texts.length == fonts.length) {
            for (int i = 0; i < texts.length; i++) {
                addText(texts[i], fonts[i]);
            }
        }
        return this;
    }

    public PdfParagraphBuilder withSpacingAfter(float spacing) {
        this.spacingAfter = spacing;
        return this;
    }


    public PdfParagraphBuilder withSpacingBefore(float spacing) {
        this.spacingBefore = spacing;
        return this;
    }

    public PdfParagraphBuilder withSpacing(float spacing) {
        this.spacingBefore = spacing;
        this.spacingAfter = spacing;
        return this;
    }

    public PdfParagraphBuilder withAlignment(int alignment) {
        this.alignment = alignment;
        return this;
    }


    public PdfParagraphBuilder centered() {
        this.alignment = Element.ALIGN_CENTER;
        return this;
    }

    public PdfParagraphBuilder rightAligned() {
        this.alignment = Element.ALIGN_RIGHT;
        return this;
    }

    public PdfParagraphBuilder justified() {
        this.alignment = Element.ALIGN_JUSTIFIED;
        return this;
    }

    /**
     * Sets left indentation
     * @param indent indentation in points
     * @return this builder for chaining
     */
    public PdfParagraphBuilder withIndentationLeft(float indent) {
        this.indentationLeft = indent;
        return this;
    }

    /**
     * Sets right indentation
     * @param indent indentation in points
     * @return this builder for chaining
     */
    public PdfParagraphBuilder withIndentationRight(float indent) {
        this.indentationRight = indent;
        return this;
    }

    /**
     * Sets first line indent
     * @param indent indentation in points
     * @return this builder for chaining
     */
    public PdfParagraphBuilder withFirstLineIndent(float indent) {
        this.firstLineIndent = indent;
        return this;
    }

    /**
     * Sets line leading (line height)
     * @param leading leading in points
     * @return this builder for chaining
     */
    public PdfParagraphBuilder withLeading(float leading) {
        this.leading = leading;
        return this;
    }

    /**
     * Adds a line separator after the paragraph
     * @param offset offset from baseline (negative moves up)
     * @return this builder for chaining
     */
    public PdfParagraphBuilder withLineSeparator(float offset) {
        this.addLineSeparator = true;
        this.lineSeparatorOffset = offset;
        return this;
    }

    /**
     * Adds a line separator after the paragraph with default offset
     * @return this builder for chaining
     */
    public PdfParagraphBuilder withLineSeparator() {
        return withLineSeparator(-3f);
    }

    /**
     * Builds and returns the configured Paragraph
     * @return the constructed Paragraph
     */
    public Paragraph build() {
        Paragraph paragraph = new Paragraph();

        paragraph.addAll(chunks);

        paragraph.setSpacingAfter(spacingAfter);
        paragraph.setSpacingBefore(spacingBefore);

        paragraph.setAlignment(alignment);

        paragraph.setIndentationLeft(indentationLeft);
        paragraph.setIndentationRight(indentationRight);
        paragraph.setFirstLineIndent(firstLineIndent);

        if (leading > 0) {
            paragraph.setLeading(leading);
        }

        return paragraph;
    }

    public Element[] buildWithElements() {
        Paragraph paragraph = build();

        if (addLineSeparator) {
            LineSeparator separator = new LineSeparator();
            separator.setOffset(lineSeparatorOffset);
            return new Element[]{paragraph, separator};
        }

        return new Element[]{paragraph};
    }

    public void addTo(Document document) throws DocumentException {
        Element[] elements = buildWithElements();
        for (Element element : elements) {
            document.add(element);
        }
    }
}