package entities;

public abstract class User {
	
	protected long phoneNumber;
    protected String email;
    
    public User(String email, long phoneNumber) {
        this.phoneNumber = phoneNumber;
        this.email = email;
    }
     
    public long getPhoneNumber() {
    	return phoneNumber; 
    }
    
    public void setPhoneNumber(long phoneNumber) {
    	this.phoneNumber = phoneNumber; 
    }
    
    public String getEmail() { 
    	return email; 
    }
    
    public void setEmail(String email) {
    	this.email = email; 
    }
}



