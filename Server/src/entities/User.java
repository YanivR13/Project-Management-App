package entities;

public abstract class User {
	
	protected String phoneNumber;
    protected String email;
    
    public User(String email, String phoneNumber) {
        this.phoneNumber = phoneNumber;
        this.email = email;
    }
     
    public String getPhoneNumber() {
    	return phoneNumber; 
    }
    
    public void setPhoneNumber(String phoneNumber) {
    	this.phoneNumber = phoneNumber; 
    }
    
    public String getEmail() { 
    	return email; 
    }
    
    public void setEmail(String email) {
    	this.email = email; 
    }
}



