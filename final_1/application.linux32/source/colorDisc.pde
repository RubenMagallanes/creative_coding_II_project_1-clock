
static int increment = 180;//how many increments in a circle

class colorDisc 
{
  private ArrayList<alarm> alrms ; 

  private color col1;
  private color col2;
  private int size;
  // in degrees - can also be used to tell what time it is 
  private float rotation; 
  private float initRot = 0;

  private int x;
  private int y;

  private String type;

  /**
   * constructor
   * takes two colours, and an int reprisenting the size 
   type: hour, min
   */
  colorDisc (color c1, color c2, int s, String type, int X, int Y)
  {
    //setters
    this.col1 = c1;
    this.col2 = c2;
    rotation = 0;
    int si = s;
    alrms = new ArrayList<alarm>();

    //  if (si < sizeMin) si = sizeMin;
    // if (si > sizeMax) si = sizeMax;
    this.size = si;
    this.type = type;

    localMillis = millis();

    if (type == "min")
    {
      float rot = map (second(), 0, 59, 0, 354);
      initRot = rot;
    }

    this.x = X;
    this.y = Y;
  }

  /**
   returns the 'current color', should be the color 
   thats 45 degrees from vertical to the left
   //SHARES SIMILAR CCODE WITH SETTIME
   */
  color askCurrentColor ()
  {
    color from = col1;
    color to = col2;  

    color col = 150;

    //2 cases 0-179, 180-359
    if (rotation >=0 && rotation < 180)
    {
      //lerp start-end  
      float amt = map (rotation, 0, 179, 0, 1);     
      col = lerpColor(from, to, amt);
    } else //if (rotation >= 180 && rotation < 360)
    {
      float amt = map (rotation, 180, 359, 0, 1);     
      col = lerpColor(to, from, amt);
    }
    return col;
  }

  void giveAlarm(alarm a)
  {
    this.alrms.add (a);
  } 
  void checkAlarms() 
  {
    for (alarm a : alrms)
    {
      int alarmTimeSec = a.askTime() * 60;
      int currTimeSec = hour()*60 * 60 + minute()*60 + second();
      int secondsTillAlarmGoesOff = alarmTimeSec - currTimeSec;
      if (secondsTillAlarmGoesOff < 0 && a.checkFlag()) 
      {        
        toRemove.add (a);
      }
    }
    for (alarm a : toRemove)
    {
      alrms .remove(a);
    }
  }
  void changecolours (color c1, color c2)
  {
    col1 = c1;
    col2 = c2;
  }

  /**
   * updates this circles time, so its rotation amount , a float.   
   */
  int localMillis; // current system millis time
  int diffMillis = 0; // millis since last time check
  int second = second();
  int mils = 0;
  void updateTime()
  {
    if (type == "hour") {
      //how many seconds have passed since 00:00
      int secdiff = hour()*60*60 + minute()*60 + second();      
      float degToRotate = map(secdiff, 0, (24*60*60), 0, 360);
      this.rotation = degToRotate;
    } else //type == min disc
    {
      diffMillis = millis() - (second()*1000);

      mils = second() * 1000 + diffMillis;

      float degToRotate = map(mils, 0, 60000, 0, 360);      
      this.rotation = degToRotate + initRot;
      //if (rotation > 359) rotation -= 360;
      //println (second() + " - " + rotation);
      localMillis = millis();
    }
  }

  float askRotation ()
  {
    return this.rotation;
  }

  void render (color fc)
  {
    pushStyle();
    strokeWeight((int)width/100 +1);
    //-----init---------//

    pushMatrix();// save state on stack for restoring later
    // translate (width*0.5, height*0.5); // middle is now 0,0
    translate(x, y);

    //calculate rotate amount, the first line drawen is where zero is
    float extraRotate = map(rotation, 0, 359, 0, TWO_PI);
    rotate (radians(180));
    //---------plus or minus determines the direction of rotation
    rotate (radians(225) - extraRotate); // rotate
  
    

      //---------draw circle loop---------//

    color from = col1;
    color to = col2;    
    //calculate rotation increment amount
    float rotamt = map (1, 0, increment-1, 0, PI); // static 

    //in two loops for both halves
    for (int i = 0; i< increment; i++)
    {      
      float amt = map (i, 0, increment, 0, 1);     
      color curr = lerpColor(from, to, amt);
      stroke (curr);
      line(0, 0, size, 0);
      rotate (rotamt);
    }
    for (int i = 0; i< increment-1; i++)
    {      
      float amt = map (i, 0, increment, 0, 1);     
      color curr = lerpColor(to, from, amt);
      stroke (curr);
      line(0, 0, size, 0);
      rotate (rotamt);
    }
    //circle is now drawn at correct rotation

    //----------------draw little bits and bobs
    noFill();
    stroke(askCurrentColor());
    strokeWeight(5);
    if (type == "hour")
    {
    //  pushStyle();
   // stroke (255);
  //  line (0,0,1000,0);
    //popStyle();
    
    
      ellipse(0, 0, (size*2)+2, (size*2)+2);
      //draw alarms on it 
      if (this.alrms.size() > 0)
      {
        for (alarm a : alrms)
        {
          int alrmTime = a.askTime();//0-1499
          //draws them in font col
          pushStyle();
          pushMatrix();
          fill(fontcol);
          stroke(fc);
          float rotateamt = map (alrmTime, 0, 1440, 0, (2*PI)-(radians(1)));
          float transamt = 105;

          
         // println("alrmTime: " + alrmTime+", mapped to:" +rotateamt);
         //print(radians(1));
          rotate (rotateamt);
        
          translate (transamt, 0);
       //println(transamt);
          ellipse (0, 0, 1, 1);
          popMatrix();
          popStyle();
        }
      }
    } else //if mindisk
    {
      if (this.alrms.size() > 0)
      {
        strokeWeight(15);
        ellipse(0, 0, (100), (100));
      }
    }

    //----reset stack -----//
    popMatrix();
    popStyle();
  }
}