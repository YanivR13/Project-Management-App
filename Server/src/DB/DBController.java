package DB;

import java.util.List;

/**
 * Controller responsible for data persistence, including saving, 
 * retrieving, and searching for all system entities.
 */
public class DBController {
	
	/**
     * Purpose: Saves an entity object into the system storage.
     * Receives: Object entity (e.g., a new Reservation or Table).
     * Returns: boolean (true if the save operation was successful).
     */
    public boolean save(Object entity) {
        // Logic to identify the object type and add it to the corresponding list
        return false;
    }
    
    /**
    * Purpose: Retrieves all stored entities of a specific type.
    * Receives: Class<T> entityType (e.g., Table.class).
    * Returns: List<T> (a list containing all instances of that type).
    */
   public <T> List<T> getAll(Class<T> entityType) {
       // Logic to filter the central storage and return all objects of the requested class
       return null;
   }
   
   /**
    * Purpose: Finds a specific entity using its unique identifier.
    * Receives: Class<T> entityType, Object id.
    * Returns: T (the found object, or null if it doesn't exist).
    */
   public <T> T findById(Class<T> entityType, Object id) {
       // Logic to iterate through the relevant list and match the ID field
       return null;
   }
   
   /**
    * Purpose: Deletes a specific entity from the system.
    * Receives: Object entity.
    * Returns: boolean (true if the entity was found and removed).
    */
   public boolean delete(Object entity) {
       // Search for the object in the storage and remove it
       return false;
   }

}
