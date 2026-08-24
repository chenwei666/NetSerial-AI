package com.chenwei666.netserial.change;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ChangePdfExporter {
    private static final int PAGE_WIDTH = 595;
    private static final int PAGE_HEIGHT = 842;
    private static final float MARGIN = 36f;
    private static final float LINE_HEIGHT = 14f;

    public void write(ChangeTask task, OutputStream output) throws IOException {
        Objects.requireNonNull(task, "task");
        Objects.requireNonNull(output, "output");
        PdfDocument document = new PdfDocument();
        try {
            Paint body = new Paint(Paint.ANTI_ALIAS_FLAG);
            body.setColor(Color.BLACK);
            body.setTextSize(10f);
            body.setTypeface(Typeface.MONOSPACE);
            List<String> lines = wrap(new ChangeEvidenceFormatter().toMarkdown(task), body,
                    PAGE_WIDTH - 2 * MARGIN);
            int pageNumber = 1;
            int index = 0;
            while (index < lines.size() || pageNumber == 1) {
                PdfDocument.Page page = document.startPage(new PdfDocument.PageInfo.Builder(
                        PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create());
                Canvas canvas = page.getCanvas();
                Paint title = new Paint(body);
                title.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                title.setTextSize(13f);
                canvas.drawText("NetSerial AI - Change Evidence", MARGIN, MARGIN, title);
                float y = MARGIN + 24f;
                while (index < lines.size() && y < PAGE_HEIGHT - MARGIN - LINE_HEIGHT) {
                    String line = lines.get(index++);
                    Paint paint = line.startsWith("#") ? title : body;
                    canvas.drawText(line, MARGIN, y, paint);
                    y += line.isEmpty() ? LINE_HEIGHT / 2f : LINE_HEIGHT;
                }
                Paint footer = new Paint(body);
                footer.setTextSize(8f);
                footer.setColor(Color.DKGRAY);
                canvas.drawText("Page " + pageNumber, PAGE_WIDTH - MARGIN - 40f,
                        PAGE_HEIGHT - 18f, footer);
                document.finishPage(page);
                pageNumber++;
            }
            document.writeTo(output);
        } finally {
            document.close();
        }
    }

    private static List<String> wrap(String text, Paint paint, float maximumWidth) {
        List<String> result = new ArrayList<>();
        for (String source : text.replace('\r', '\n').split("\\n", -1)) {
            if (source.isEmpty()) {
                result.add("");
                continue;
            }
            String remaining = source;
            while (!remaining.isEmpty()) {
                int count = paint.breakText(remaining, true, maximumWidth, null);
                if (count <= 0) count = 1;
                result.add(remaining.substring(0, count));
                remaining = remaining.substring(count);
            }
        }
        return result;
    }
}
