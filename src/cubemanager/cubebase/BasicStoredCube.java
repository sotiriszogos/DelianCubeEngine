/*
*    DelianCubeEngine. A simple cube query engine.
*    Copyright (C) 2018  Panos Vassiliadis
*
*    This program is free software: you can redistribute it and/or modify
*    it under the terms of the GNU Affero General Public License as published
*    by the Free Software Foundation, either version 3 of the License, or
*    (at your option) any later version.
*
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU Affero General Public License for more details.
*
*    You should have received a copy of the GNU Affero General Public License
*    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*
*/


package cubemanager.cubebase;

import java.io.Serializable;

import cubemanager.starschema.FactTable;

public class BasicStoredCube extends Cube implements Serializable{
	private static final long serialVersionUID = 4390482518182625971L;

    
	/**
	 * @uml.property  name="fCtbl"
	 * @uml.associationEnd  
	 */
	private FactTable FCtbl;
        
	public BasicStoredCube(String NAME) {
			super(NAME);
	}
	 
	public void setFactTable(FactTable Factbl){
		FCtbl=Factbl;
	}

	public FactTable FactTable() {
		return FCtbl;
	}

}
