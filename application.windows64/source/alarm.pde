

public class alarm
{
  private int time;
  private String message;

  boolean hasTriggered = false; //flag = true when the alarm has gone 
                                //off, signalling its ok to be deleted
  /**
   takes:    
   int-    time alarm is set to in minutes
   string- text associated with the alarm 
   */
  public alarm (int set_time, String msg)
  {
    this.time = set_time;
    this.message = msg;
  }

  public int askTime()
  {
    return this.time;
  }
  public String askMessage ()
  {
    return this.message;
  }
  void trigger ()
  {
    hasTriggered = true;
  }
  boolean checkFlag ()
  {
    return hasTriggered;
  }
}