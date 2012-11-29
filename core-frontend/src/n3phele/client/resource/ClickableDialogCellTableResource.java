/**
 * @author Nigel Cook
 *
 * (C) Copyright 2010-2011. All rights reserved.
 */
package n3phele.client.resource;

import com.google.gwt.user.cellview.client.CellTable;

public interface ClickableDialogCellTableResource  extends CellTable.Resources { 
	public interface CellTableStyle extends CellTable.Style {}; 
	@Source({"ClickableDialogCellTable.css"}) 
	CellTableStyle cellTableStyle(); 
}