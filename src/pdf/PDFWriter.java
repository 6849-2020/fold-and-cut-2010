package pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class PDFWriter {
	private static class PositionReportingStream extends OutputStream {
		OutputStream stream;
		int pos;
		
		public PositionReportingStream(OutputStream stream) {
			this.stream = stream;
			this.pos = 0;
		}
		
		public int getPos() {
			return this.pos;
		}
		
		@Override
		public void close() throws IOException {
			this.stream.close();
		}
		
		@Override
		public void flush() throws IOException {
			this.stream.flush();
		}

		@Override
		public void write(int b) throws IOException {
			this.stream.write(b);
			pos++;
		}
		
		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			this.stream.write(b, off, len);
			pos += len;
		}
		
		@Override
		public void write(byte[] b) throws IOException {
			this.stream.write(b);
			pos += b.length;
		}
	}
	private PositionReportingStream stream;
	
	private List<Integer> objectOffsets = new ArrayList<Integer>();
	
	public PDFWriter(OutputStream stream) throws IOException {
		this.stream = new PositionReportingStream(stream);
		objectOffsets.add(0); // Dummy object.
		writeHeader();
	}
	
	public int nextObjectId() {
		return objectOffsets.size();
	}
	
	private static byte[] toAscii(String s) {
		try {
			return s.getBytes("US-ASCII");
		} catch (UnsupportedEncodingException e) {
			// This should never ever ever happen.
			throw new RuntimeException(e.getMessage());
		}
	}
	
	private void writeString(String s) throws IOException {
		this.stream.write(toAscii(s));
	}
	
	private void writeHeader() throws IOException {
		writeString("%PDF-1.7\n");
	}
	
	public int appendStream(String streamDataAsString) throws IOException {
		return appendStream(toAscii(streamDataAsString));
	}
	
	public int appendStream(byte[] streamData) throws IOException {
		byte[] streamHeader = toAscii("<< /Length " + streamData.length + " >>\n");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		baos.write(streamHeader);
		baos.write(toAscii("stream\n"));
		baos.write(streamData);
		baos.write(toAscii("\nendstream"));
		return appendObject(baos.toByteArray());
	}

	public int appendObject(String objectDataAsString) throws IOException {
		return appendObject(toAscii(objectDataAsString));
	}
	
	public int appendObject(byte[] objectData) throws IOException {
		int offset = this.stream.getPos();
		int objectId = objectOffsets.size();
		objectOffsets.add(offset);
		
		StringBuilder sb = new StringBuilder();
		sb.append(objectId);
		sb.append(" 0 obj\n");
		writeString(sb.toString());
		
		this.stream.write(objectData);
		
		writeString("\nendobj\n");
		return objectId;
	}
	
	public void close(int catalogId) throws IOException {
		int xrefOffset = writeXRef();
		writeTrailer(xrefOffset, catalogId);
		this.stream.close();
	}
	
	private int writeXRef() throws IOException {
		int offset = stream.getPos();
		DecimalFormat df = new DecimalFormat("0000000000");
		writeString("xref\n");
		writeString("0 " + objectOffsets.size() + "\n");
		writeString("0000000000 65535 f\n"); // Free object linked list is empty.
		for (int i = 1; i < objectOffsets.size(); i++) {
			writeString(df.format(objectOffsets.get(i)) + " 00000 n\n");
		}
		return offset;
	}
	
	private void writeTrailer(int xrefOffset, int catalogId) throws IOException {
		writeString("trailer\n");
		// TODO: This API really really sucks.
		writeString("<<\n");
		writeString("  /Size " + objectOffsets.size() + "\n");
		writeString("  /Root " + catalogId + " 0 R\n");
		writeString(">>\n");
		writeString("startxref\n");
		writeString(xrefOffset + "\n");
		writeString("%%EOF\n");
	}
}
