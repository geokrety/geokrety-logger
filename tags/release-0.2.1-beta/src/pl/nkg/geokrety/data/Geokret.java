/*
 * Copyright (C) 2013 Michał Niedźwiecki
 * 
 * This file is part of GeoKrety Logger
 * http://geokretylog.sourceforge.net/
 * 
 * GeoKrety Logger is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * or see <http://www.gnu.org/licenses/>
 */
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
