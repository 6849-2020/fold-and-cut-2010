package foldandcut;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import straightskeleton.Corner;
import straightskeleton.Edge;
import straightskeleton.Output;

public class SkeletonOutput {
	private SkeletonInput input;
	private Output ssOutput;
	private Map<Output.Face, SkeletonFace> faces;
	private Map<Edge, SkeletonFace> cutEdgeToFace;
	
	public SkeletonOutput(SkeletonInput input, Output ssOutput) {
		this.input = input;
		this.ssOutput = ssOutput;
		this.faces = new HashMap<Output.Face, SkeletonFace>();
		this.cutEdgeToFace = new HashMap<Edge, SkeletonFace>();
		for (Output.Face f : ssOutput.faces.values()) {
			// Apparently this creates them sometimes? We'll just ignore them I guess.
			if (f.points.size() == 0)
				continue;
			SkeletonFace sf = new SkeletonFace(this, f);
			this.faces.put(f, sf);
			for (Corner c : f.definingCorners) {
				this.cutEdgeToFace.put(c.nextL, sf);
			}
		}
	}
	
	public SkeletonInput getInput() {
		return input;
	}
	
	public Output getOutput() {
		return ssOutput;
	}
	
	public Map<Output.Face, SkeletonFace> getFaces() {
		return Collections.unmodifiableMap(faces);
	}
	
	public SkeletonFace getFaceForCutEdge(Edge edge) {
		return cutEdgeToFace.get(edge);
	}
}
