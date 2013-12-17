package pl.nkg.geokrety.data;

import org.w3c.dom.Node;

public class Geokret {
	private int id;
	private int dist;
	private int owner_id;
	private Integer state;
	private int type;
	private String name;
	private String nr;
	
	public Geokret(Node node) {
		id = Integer.parseInt(node.getAttributes().getNamedItem("id").getTextContent());
		dist = Integer.parseInt(node.getAttributes().getNamedItem("dist").getTextContent());
		owner_id = Integer.parseInt(node.getAttributes().getNamedItem("owner_id").getTextContent());
		type = Integer.parseInt(node.getAttributes().getNamedItem("type").getTextContent());
		nr = node.getAttributes().getNamedItem("nr").getTextContent();
	    name = node.getTextContent();
	    
	    Node stateNode = node.getAttributes().getNamedItem("state");
	    if (stateNode == null) {
	    	state = null;
	    } else {
	    	state = Integer.parseInt(stateNode.getTextContent());
	    }
	}

	public int getID() {
		return id;
	}

	public int getDist() {
		return dist;
	}

	public int getOwnerID() {
		return owner_id;
	}

	public Integer getState() {
		return state;
	}

	public int getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public String getTackingCode() {
		return nr;
	}
	
	@Override
	public String toString() {
		return name + " (" + nr + ")";
	}
}
