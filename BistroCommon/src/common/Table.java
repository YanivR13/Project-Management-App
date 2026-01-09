package common;

import java.io.Serializable;

public class Table  implements Serializable{
	
    private static final long serialVersionUID = 1L;

	
	private int tableId;
	private int capacity;
	private boolean isAvailable;
	
	public Table(int tableId, int capacity, boolean isAvailable) {
		this.tableId=tableId;
		this.capacity=capacity;
		this.isAvailable = isAvailable;	
	}
	public int getTableId() {
		return tableId;
	}
	public void setTableId(int tableId) {
		this.tableId = tableId;
	}
	public int getCapacity() {
		return capacity;
	}
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
	public boolean isAvailable() {
		return isAvailable;
	}
	public void setAvailabe(boolean isAvailable) {
		this.isAvailable = isAvailable;
	}
	
	

}
