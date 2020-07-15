package it.course.myblog.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import java.util.List;
import java.util.stream.Collectors;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import org.springframework.stereotype.Service;

import it.course.myblog.entity.Post;

@Service
public class PDFService {

	// attributi PDF
	private static Font FONT_TITLE = new Font(Font.FontFamily.TIMES_ROMAN, 16, Font.BOLD, new BaseColor(0, 102, 204));
	private static Font FONT_CONTENT = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL);
	private static Font FONT_AUTHOR = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.ITALIC);
	private static Font FONT_DATE = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.ITALIC, BaseColor.DARK_GRAY);

	public InputStream createPdfFromPost(Post p) throws Exception {

		String author = p.getAuthor().getUsername();
		String title = p.getTitle();
		String content = p.getContent();
		double average = p.getAvgRating();
		String createdAt = String.format("%1$tY-%1$tm-%1$td", p.getCreatedAt());

		List<String> tagList = p.getTags().stream().map(t -> t.getTagName()).collect(Collectors.toList());

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		Document document = new Document(PageSize.A4, 50, 50, 50, 50);
		PdfWriter.getInstance(document, out);

		document.open();

		// Add metadata
		addMetaData(document, title, author, tagList.toString());

		// Title
		Paragraph pTitle = new Paragraph(title, FONT_TITLE);
		pTitle.setAlignment(Element.ALIGN_LEFT);
		addEmptyLine();
		document.add(pTitle);

		// Image
		if (!(p.getDbFile() == null)) {
			Image img = Image.getInstance(p.getDbFile().getData());
			img.setBorder(3);
			addEmptyLine();
			document.add(img);
		}

		// Content
		Paragraph pContent = new Paragraph(content, FONT_CONTENT);
		pContent.setAlignment(Element.ALIGN_JUSTIFIED);
		addEmptyLines(pContent, 2);
		document.add(pContent);
		/*
		 * PdfPTable table = new PdfPTable(3); table.setWidthPercentage(100);
		 * 
		 * PdfPCell authorCell = new PdfPCell(new Phrase("Author: "+author,
		 * FONT_AUTHOR)); authorCell.setBorder(Rectangle.NO_BORDER);
		 * table.addCell(authorCell);
		 * 
		 * PdfPCell averageCell = new PdfPCell(new
		 * Phrase("Average: "+Double.toString(average), FONT_AUTHOR));
		 * averageCell.setBorder(Rectangle.NO_BORDER); table.addCell(averageCell);
		 * 
		 * PdfPCell createdAtCell = new PdfPCell(new Phrase("Created at: "+createdAt,
		 * FONT_AUTHOR)); createdAtCell.setBorder(Rectangle.NO_BORDER);
		 * table.addCell(createdAt); document.add(table);
		 */

		// Author
		Paragraph pAuthor = new Paragraph("Author: " + author, FONT_AUTHOR);
		pAuthor.setAlignment(Element.ALIGN_LEFT);
		addEmptyLine();
		document.add(pAuthor);

		// Created At
		Paragraph pCreatedAt = new Paragraph("Date: " + createdAt, FONT_DATE);
		pCreatedAt.setAlignment(Element.ALIGN_LEFT);
		addEmptyLine();
		document.add(pCreatedAt);

		// Average
		Paragraph pAverage = new Paragraph("Average: " + average, FONT_AUTHOR);
		pAverage.setAlignment(Element.ALIGN_RIGHT);
		addEmptyLine();
		document.add(pAverage);

		document.close();

		InputStream in = new ByteArrayInputStream(out.toByteArray());

		return in;
	}

	private static void addEmptyLines(Paragraph paragraph, int number) {
		for (int i = 0; i < number; i++) {
			paragraph.add(new Paragraph(" "));
		}
	}

	private static Paragraph addEmptyLine() {
		return new Paragraph(" ");
	}

	private void addMetaData(Document document, String title, String author, String tagList) {
		document.addTitle(title);
		document.addKeywords(tagList);
		document.addAuthor(author);
	}

}
