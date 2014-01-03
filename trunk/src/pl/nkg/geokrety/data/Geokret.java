/*
 * Copyright (C) 2013, 2014 Michał Niedźwiecki
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
	private boolean sticky = false;

	public Geokret(Node node) {
		id = Integer.parseInt(node.getAttributes().getNamedItem("id")
				.getNodeValue());
		dist = Integer.parseInt(node.getAttributes().getNamedItem("dist")
				.getNodeValue());
		owner_id = Integer.parseInt(node.getAttributes()
				.getNamedItem("owner_id").getNodeValue());
		type = Integer.parseInt(node.getAttributes().getNamedItem("type")
				.getNodeValue());
		nr = node.getAttributes().getNamedItem("nr").getNodeValue();
		name = node.getChildNodes().item(0).getNodeValue();
		// getTextContent() getNodeValue();

		Node stateNode = node.getAttributes().getNamedItem("state");
		if (stateNode == null) {
			state = null;
		} else {
			state = Integer.parseInt(stateNode.getNodeValue());
		}
	}

	public Geokret(int id, int dist, int owner_id, Integer state, int type,
			String name, String nr, boolean sticky) {
		super();
		this.id = id;
		this.dist = dist;
		this.owner_id = owner_id;
		this.state = state;
		this.type = type;
		this.name = name;
		this.nr = nr;
		this.sticky = sticky;
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

	public boolean isSticky() {
		return sticky;
	}

	public void setSticky(boolean sticky) {
		this.sticky = sticky;
	}
}
