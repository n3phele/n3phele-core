/**
 * @author Nigel Cook
 *
 * (C) Copyright 2010-2011. All rights reserved.
 */
package n3phele.client.resource;

import com.google.gwt.user.cellview.client.DataGrid;

public interface DataGridResource extends DataGrid.Resources {
	interface DataGridStyle extends DataGrid.Style {};
	@Source({"DataGrid.css"})
	DataGridStyle dataGridStyle();
}