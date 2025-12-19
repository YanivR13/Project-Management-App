package entities;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Restaurant {
	
	private String name;
	private Map<DayOfWeek, TimeRange> regularHours;
	private Map<LocalDate, TimeRange> specialHours;
	private List<Table> tables;
	
	public Restaurant(String name) {
        this.name = name;
        this.regularHours = new EnumMap<>(DayOfWeek.class);
        this.specialHours = new HashMap<>();
        this.tables = new ArrayList<>();
    }
	
	public void addTable(Table table) {
        tables.add(table);
    }

    public List<Table> getTables() {
        return tables;
    }
	
	public void setRegularHours(DayOfWeek day, LocalTime open, LocalTime close) {
        regularHours.put(day, new TimeRange(open, close));
    }
	
	public void setSpecialHours(LocalDate date, LocalTime open, LocalTime close) {
        specialHours.put(date, new TimeRange(open, close));
    }
	
	public TimeRange getHoursForDate(LocalDate date) {
        if (specialHours.containsKey(date)) {
            return specialHours.get(date);
        }
        return regularHours.get(date.getDayOfWeek());
    }
	
	public Table getTableById(int tableId) {
	    for (Table t : tables) {
	        if (t.getTableID() == tableId) return t;
	    }
	    return null;
	}
	
	public void removeTable(int tableId) {
		for (int i = tables.size() - 1; i >= 0; i--) {
	        if (tables.get(i).getTableID() == tableId) {
	            tables.remove(i);
	        }
	    }
	}
}
