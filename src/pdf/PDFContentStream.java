package pdf;

import java.awt.Color;
import java.awt.geom.AffineTransform;

import javax.vecmath.Point2d;

public class PDFContentStream {
	private StringBuilder sb;
	
	public PDFContentStream() {
		sb = new StringBuilder();
	}
	
	private void arg(double d) {
		sb.append(d);
		sb.append(" ");
	}
	
	private void cmd(String s) {
		sb.append(s);
		sb.append("\n");
	}
	
	public void setStrokeColor(Color c) {
		arg(c.getRed() / 255.0);
		arg(c.getGreen() / 255.0);
		arg(c.getBlue() / 255.0);
		cmd("RG");
	}
	
	public void setFillColor(Color c) {
		arg(c.getRed() / 255.0);
		arg(c.getGreen() / 255.0);
		arg(c.getBlue() / 255.0);
		cmd("rg");
	}
	
	public void moveTo(Point2d p) {
		arg(p.x); arg(p.y); cmd("m");
	}
	
	public void lineTo(Point2d p) {
		arg(p.x); arg(p.y); cmd("l");
	}
	
	public void stroke() {
		cmd("S");
	}
	
	public void closeAndStroke() {
		cmd("s");
	}
	
	public void applyTransform(AffineTransform tf) {
		double[] ds = new double[6];
		tf.getMatrix(ds);
		for (double d : ds) {
			arg(d);
		}
		cmd("cm");
	}
	
	public String toString() {
		return sb.toString();
	}
}
