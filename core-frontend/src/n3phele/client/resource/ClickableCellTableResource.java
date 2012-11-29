/**
 * @author Nigel Cook
 *
 * (C) Copyright 2010-2011. All rights reserved.
 */
package n3phele.client.resource;

import com.google.gwt.user.cellview.client.CellTable;

public interface ClickableCellTableResource extends CellTable.Resources { 
	public interface CellTableStyle extends CellTable.Style {}; 
	@Source({"ClickableCellTable.css"}) 
	CellTableStyle cellTableStyle(); 
}