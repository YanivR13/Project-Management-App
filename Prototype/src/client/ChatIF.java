package client;

/**
 * הממשק הזה הוא "החוזה".
 * הוא אומר: כל מי שרוצה להיות ממשק משתמש (GUI או Console),
 * חייב לממש את הפונקציה display כדי שנוכל להציג לו הודעות.
 */
public interface ChatIF {
    
    // פונקציה להצגת הודעה למשתמש
    public abstract void display(String message);
}