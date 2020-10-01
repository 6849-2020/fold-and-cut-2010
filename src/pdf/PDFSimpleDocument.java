package pdf;

import java.io.IOException;
import java.io.OutputStream;

public class PDFSimpleDocument {
	
	public static void writeSinglePage(OutputStream stream, int mediaBoxWidth, int mediaBoxHeight, String contentStream) throws IOException {
		PDFWriter pdfWriter = new PDFWriter(stream);
		int contentsId = pdfWriter.appendStream(contentStream);
		int p = pdfWriter.nextObjectId(); // Guh
		int pageId = pdfWriter.appendObject(
				"<<\n" +
				"  /Type /Page\n" +
				"  /Parent " + (p+1) + " 0 R\n" +
				"  /Resources << >>\n" +
				"  /MediaBox [0 0 " + mediaBoxWidth + " " + mediaBoxHeight + "]\n" +
				"  /Contents " + contentsId + " 0 R\n" +
				">>");
		int pagesId = pdfWriter.appendObject(
				"<<\n" +
				"  /Type /Pages\n" +
				"  /Kids [ " + pageId + " 0 R ]\n" +
				"  /Count 1\n" +
				">>");
		int catalogId = pdfWriter.appendObject(
				"<<\n" +
				"  /Type /Catalog\n" +
				"  /Pages " + pagesId + " 0 R\n" +
				">>");
		pdfWriter.close(catalogId);
	}
	
	
}
