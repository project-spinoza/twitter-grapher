package com.tg.graph;

import uk.ac.ox.oii.sigmaexporter.model.GraphElement;

public class SigmaEdge extends GraphElement {
			
		private String label;
		private String source;
		private String target;
	    private String id;
		
		public SigmaEdge(String id) {
			super();
	        this.id=id;
			label="";
			source="";
			target="";
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public String getSource() {
			return source;
		}

		public void setSource(String source) {
			this.source = source;
		}

		public String getTarget() {
			return target;
		}

		public void setTarget(String target) {
			this.target = target;
		}
		public String getId(){
			return this.id;
		}
}
