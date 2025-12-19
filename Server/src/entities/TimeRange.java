package entities;

import java.time.LocalTime;

public class TimeRange {
	
	private LocalTime openTime;
    private LocalTime closeTime;

    public TimeRange(LocalTime open, LocalTime close) {
        this.openTime = open;
        this.closeTime = close;
    }

    @Override
    public String toString() {
        return openTime + " - " + closeTime;
    }
    
    public LocalTime getOpenTime() {
    	return openTime; 
    }
    
    public LocalTime getCloseTime() {
    	return closeTime; 
    }

}
