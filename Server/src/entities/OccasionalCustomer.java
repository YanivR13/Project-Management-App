package entities;

public class OccasionalCustomer extends User{
	
	public OccasionalCustomer(String email) {
		super(email, -1);
    }
    
    public OccasionalCustomer(long phoneNumber) {
        super(null, phoneNumber);
    }
    
    public OccasionalCustomer(String email, long phoneNumber) {
        super(email, phoneNumber);
    }
}
