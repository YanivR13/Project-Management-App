package entities;

public class OccasionalCustomer extends User{
    public OccasionalCustomer(String email, String phoneNumber) {
        super(email, phoneNumber);
    }
	
	public OccasionalCustomer byEmail(String email) {
		return new OccasionalCustomer(email, null);
    }
    
    public OccasionalCustomer byPhone(String phoneNumber) {
    	return new OccasionalCustomer(null, phoneNumber);
    }
}
