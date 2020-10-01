package foldandcut;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import graph.BoundaryHierarchyTree;
import graph.OrientedLoop;

public class FoldAndCutProblem {
	private List<SkeletonInput> skeletonInputs;
	Map<OrientedLoop, SkeletonInput> loopToInput;
	
	private static void gatherStraightSkeletonFaces(BoundaryHierarchyTree.Node node, List<SkeletonInput> output, double epsilon) {
    	if (node == null)
    		return;
    	if (node.getLoop().isInside()) {
    		// We've got one!
    		output.add(new SkeletonInput(node, epsilon));
    	}
    	for (BoundaryHierarchyTree.Node child : node.getChildren()) {
    		gatherStraightSkeletonFaces(child, output, epsilon);
    	}
    }
	
	public FoldAndCutProblem(BoundaryHierarchyTree tree, double epsilon) {
		List<SkeletonInput> skeletonInputs = new ArrayList<SkeletonInput>();
		gatherStraightSkeletonFaces(tree.getRoot(), skeletonInputs, epsilon);

		this.skeletonInputs = skeletonInputs;
		
		this.loopToInput = new HashMap<OrientedLoop, SkeletonInput>();
		for (SkeletonInput input : skeletonInputs) {
			input.parent = this; // Bah.
			BoundaryHierarchyTree.Node node = input.getBHTNode();
			// Add the boundary,
			loopToInput.put(node.getLoop(), input);
			// And then each of the holes.
			for (BoundaryHierarchyTree.Node child : node.getChildren()) {
				assert !child.getLoop().isInside();
				if (child.getLoop().isInside()) {
					// Should not happen!
					continue;
				}
				loopToInput.put(child.getLoop(), input);
			}
		        
		}
	}
	
	public List<SkeletonInput> getSkeletonInputs() {
		return skeletonInputs;
	}
	
	public FoldAndCutPattern computePattern() {
		List<SkeletonOutput> outputs = new ArrayList<SkeletonOutput>();
		for (SkeletonInput input : getSkeletonInputs()) {
			outputs.add(input.computeStraightSkeleton());
		}
		return new FoldAndCutPattern(this, outputs);
	}
}
