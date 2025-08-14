package com.zklcsoftware.aimodel.util;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.options.MutableDataSet;
import com.itextpdf.html2pdf.HtmlConverter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MarkdownToPdf {

    public static void main(String[] args) {
        // Markdown input content
        String markdownContent = "# Hello World\n" +
                "This is a simple **Markdown** to PDF converter in Java.\n" +
                "## Features\n" +
                "- Converts Markdown to HTML\n" +
                "- Converts HTML to PDF";

        try {
            // Step 1: Convert Markdown to HTML
            String htmlContent = markdownToHtml(markdownContent);

            // Step 2: Write HTML content to a temporary file
            File tempHtmlFile = createTempHtmlFile(htmlContent);

            // Step 3: Convert HTML to PDF
            convertHtmlToPdf(tempHtmlFile, new File("output.pdf"));

            System.out.println("PDF generated successfully!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String markdownToHtml(String markdown) {
        MutableDataSet options = new MutableDataSet();
        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        return renderer.render(parser.parse(markdown));
    }

    private static File createTempHtmlFile(String htmlContent) throws IOException {
        File tempHtmlFile = File.createTempFile("temp", ".html");
        try (FileWriter writer = new FileWriter(tempHtmlFile)) {
            writer.write(htmlContent);
        }
        return tempHtmlFile;
    }

    private static void convertHtmlToPdf(File htmlFile, File pdfFile) {
        try {
            HtmlConverter.convertToPdf(htmlFile, pdfFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
