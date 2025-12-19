package entities;

public class Table {
	
	private final int tableID;
	private int capacity;
	boolean isAvailable;
	
	public Table(int tableID, int capacity) {
		this.tableID = tableID;
		this.capacity = capacity;
		this.isAvailable = true;
	}

	public int getTableID() {
		return tableID;
	}

	public boolean isAvailable() {
		return isAvailable;
	}

	public void setAvailable(boolean isAvailable) {
		this.isAvailable = isAvailable;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

}
