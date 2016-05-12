import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import static javax.swing.JOptionPane.*; 
import javax.swing.*; 
import java.util.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class final_1 extends PApplet {





colorDisc minDisc;
colorDisc hourDisc;
//---------COLOUR PALLETTE --------------
int night1 = 0xff001848;
int night2 = 0xff483078;
int nightFont = 0xff604878;

int morning1 =0xffbae4e5;
int morning2 = 0xfff7b354;
int morningFont = 0xfff86254;

int day1 = 0xff70bcff;
int day2 = 0xffFFEFD6;
int dayFont = 0xff7C7C7C;

int sunset1 = 0xff6c1b21;
int sunset2 = 0xff253f64;
int sunsetFont = 0xfffef0a5;

//------------------ other fields -----------
PFont f;
int fontcol = dayFont;
String curr_time;
boolean override = false;
String curr_time_override;
PVector mouseCoords;
boolean mouse_over_corner = false;

ArrayList <alarm> alarms ;
ArrayList <alarm> toMin ; 
boolean alarmMode;
String alarmTim = "00:00";
String alarmMsg = "";

boolean documentation = false;
//---------- init & draw loop -----------
public void setup ()
{
  

  minDisc = new colorDisc (day2, day1, 130, "min", 250, 250);
  hourDisc = new colorDisc (day1, day2, 220, "hour", 0, 0);

  initColorDiscs ();
  curr_time_override = "night";
  f =createFont("Arial", 16, true);
  frameRate(30);
  
  alarmMode = false;

  alarms = new ArrayList <alarm>();
  toMin = new ArrayList <alarm>();
  curr_time = hour() + ":" + minute();
  mouseCoords = new PVector (0, 0);

  timeSlice();
}

int alarmTimeSec = 0; 
int currTimeSec = 0;
ArrayList<alarm> toRemove = new ArrayList<alarm>();
ArrayList<alarm> withinMin = new ArrayList<alarm>();
public void draw ()
{

  if (documentation)
  {
    background (50);
    fill(200);
    textFont(f, 15);
    text ("The 'Relax' clock. designed to stop you worrying about individual \nminutes in a day.", 10, 30);
    text ("It is a purposefully vague clock, giving only a rough indication of \nthe current minute or hour.", 10, 75);//why color/design
    text ("This clock was designed to be both less than and more than a clock or \ntimepiece. While this clock does allow"+
    " you to keep track of the \nimportant times of your day, when those said times aren't urgently soon, \nit is more like"+
    " an art-piece and exists only to please the eye and provide \ncolour.", 10, 130);
    text ("",10,200);
    text ("To set the time for a reminder, click on the solid colour dot in the \nbottom right corner of the clock face. When"+
    " it reaches the specified time,\nthe clock will display a popup with the users message.", 10, 275);//explain 
 
    text ("Keyboard Controls: 'q','w','e','r' override the time of day detected from the \ncurrent time, so you can see the different"+
    " colour palettes without \nyou having to change the system clock.", 10, 350);
    text ("'t' removes the override, allowing the colour palette to be determined \nby the time of day",10,415);
  } else {
    checkMouseOver();
    scale (2.0f);
    //for testing 
    //translate (150, 150);
    // scale(0.5);

    if (override)
    {
      timeSlice(curr_time_override);
    } else
    {
      timeSlice();
    } // change curr color based on curr time
    background(hourDisc.askCurrentColor());

    updateTime();//change curr time var, feeds time to discs

    //to calculations
    //all alarms in main list get put in hour disc and minute arraylist
    toRemove.clear();
    for (alarm a : alarms)
    {
      // println("alarm in alarms list");
      // marks alarm fro removal from main list, 
      // adds alarm to hour disc for rendering
      // adds to different list to wait to be drawen on minute disc  
      toRemove.add (a);
      hourDisc.giveAlarm (a);
      toMin.add (a);
    }
    for (alarm a : toRemove)
    {
      alarms.remove(a);
    }
    toRemove.clear();
    currTimeSec = hour()*60 * 60 + minute()*60 + second();
    for (alarm a : toMin)
    {
      alarmTimeSec = a.askTime() * 60;

      int secondsTillAlarmGoesOff = alarmTimeSec - currTimeSec;

      // println("alarm within min: " + secondsTillAlarmGoesOff + "seconds till alarm goes off");
      if (secondsTillAlarmGoesOff < 61 && secondsTillAlarmGoesOff > 0)
      {
        //give to minute disc
        //remove from toMin array
        toRemove.add (a);
        minDisc.giveAlarm (a);
        withinMin.add(a);
      }
    }
    for (alarm a : toRemove)
    {
      toMin.remove (a);
    }
    toRemove.clear();
    //alarms should now be empty

    hourDisc.render(fontcol);
    minDisc.render(fontcol);
    drawOthers();

    //check if an alarm is going off
    for (alarm a : withinMin)
    {
      alarmTimeSec = a.askTime() * 60;
      int secondsTillAlarmGoesOff = alarmTimeSec - currTimeSec;
      if (secondsTillAlarmGoesOff == 0)
      {
        alarmNotify(a);
        toRemove.add (a);
      }
    } 
    for (alarm a : toRemove)
    {
      withinMin.remove (a);
    }

    if (alarmMode)
    {
      fill(50);
      stroke (80);
      rect (10, 10, (width/2)-20, (height/2)-20);

      textFont(f, 40);
      fill(200);
      text(alarmTim, 80, 80);
      textFont(f, 20);
      text (alarmMsg, 20, 150);
    }
  }
}


//-----------------other functions-------------------
public void alarmNotify (alarm a)
{
  //gets time and msg from alarm
  //displays on screen
  alarmMode = true;
  alarmTim = hour() + ":" + minute();
  alarmMsg = a.askMessage();

  minDisc.checkAlarms();
  hourDisc.checkAlarms ();
}

public void initColorDiscs()
{
  //total number of minutes that have passed
  int secdiff = hour()*60*60 + minute()*60 + second();

  float degToRotate = map(secdiff, 0, (24*60*60), 0, 360);
}

public void drawOthers()
{
  pushStyle ();
  pushMatrix();

  //change line depending on alarm thingie proximity


  stroke (fontcol);
  line (75, 75, (width/2)-75, (height/2)-75);
  line (75, 75, 78, 72);
  line ((width/2)-75, (height/2)-75, (width/2)-78, (height/2)-72);
  fill(fontcol);
  translate (width*0.5f, height*0.5f);
  strokeWeight(2);

  ellipse (0, 0, 30, 30);


  // print(mouse_over_corner == true);
  if (mouse_over_corner) // draw hover thingie
  {   
    pushStyle();
    pushMatrix();
    scale (0.85f);
    stroke (255);
    strokeWeight(2);
    fill(255, 50);
    ellipse (0, 0, width/5, height/5);

    fill(255, 100);
    // point(-36,-36);
    noStroke();

    rect (-25, -37, 12, 36);
    rect (-37, -25, 12, 12);
    rect (-13, -25, 12, 12); //TODO
    popMatrix();
    popStyle();
  }

  popMatrix();
  ellipse (0, 0, 20, 20);
  popStyle ();
}
//for keyboard overridde
public void timeSlice(String time)
{
  if (time == "night")
  {
    fontcol = nightFont;
    minDisc.changecolours (night2, night1);
    hourDisc.changecolours (night1, night2);
  } else if (time == "morning") 
  {
    fontcol = morningFont;
    minDisc.changecolours (morning2, morning1);
    hourDisc.changecolours (morning1, morning2);
  } else if (time == "day")
  {
    fontcol = dayFont;
    minDisc.changecolours (day2, day1);
    hourDisc.changecolours (day1, day2);
  } else if (time == "sunset")
  {
    fontcol = sunsetFont;
    minDisc.changecolours (sunset2, sunset1);
    hourDisc.changecolours (sunset1, sunset2);
  }
}


/**
 * checks what time of day it is
 * -night
 * -morning
 * -day
 * -sunset
 *  updates colorwheels accordingly, and font color
 */
public void timeSlice()
{


  if (hour() >20 || hour() <4)
  {//time is night
    fontcol = nightFont;
    minDisc.changecolours (night2, night1);
    hourDisc.changecolours (night1, night2);
  } else if (hour() >3 && hour() < 9) 
  {
    fontcol = morningFont;
    minDisc.changecolours (morning2, morning1);
    hourDisc.changecolours (morning1, morning2);
  } else if (hour() > 8 && hour() < 17)
  {
    fontcol = dayFont;
    minDisc.changecolours (day2, day1);
    hourDisc.changecolours (day1, day2);
  } else if (hour() > 16 && hour() < 21)
  {
    fontcol = sunsetFont;
    minDisc.changecolours (sunset2, sunset1);
    hourDisc.changecolours (sunset1, sunset2);
  }
}


/**
 * asks the system for the current time in hours and minutes, appends 
 * a zero to the minute if its a single digit
 *
 */
int second = 0;

public void updateTime ()
{
  curr_time =  hour() + ":";



  if (minute() >= 0 && minute() < 10)
  {
    // curr_time += '0';
  }
  //curr_time += minute();

  // curr_time += ":"+second();

  minDisc.updateTime();
  hourDisc.updateTime();
}



/**
 * checks weather the mouse si over the button
 */
PVector corner = new PVector (500, 500);

public void checkMouseOver()
{
  mouseCoords.set(mouseX, mouseY);
  if (mouseCoords.dist(corner) < 101)
    mouse_over_corner = true;  
  else 
    mouse_over_corner = false;
}

int user_date_minutes = 0;
public void handleCornerClicked()
{
  String date = showInputDialog 
    ("enter time of reminder\nmake sure it's in 24 hour format", "HH:MM");
  boolean valid = true;
  if (date == null) valid = false;
  if (parseUserDate(date)) //parses user date
  {
    //ask for message
    String user_message = showInputDialog("enter alarm message, dont make it too long!", "alarm message!"); 
    //deal with message
    alarm al = new alarm (user_date_minutes, user_message);
    //add to array list
    alarms.add(al);
    //print (al.askTime() + al.askMessage());

    //draw loop checks for alarms to be drawen
  } else 
  {
    //error handle
  }
}

private boolean parseUserDate (String input)
{
  try 
  {
    String[] split = input.split (":");
    if (split.length != 2) return false;
    int shour = Integer.parseInt (split[0]);
    int smin  = Integer.parseInt (split[1]);
    user_date_minutes = smin + (shour * 60);
    return true;
  }
  catch (Exception e) {
    print("in exception");
    return false;
  }
}

public void mousePressed ()
{
  if (alarmMode)
  {
    alarmMode = false;
  } else

    if (mouse_over_corner)
  {
    handleCornerClicked();
  }
}

//called each time a key is pressed
public void keyPressed ()
{
  if (key == 't' || key == 'T')
  {
    override = false;
  } else if (key == 'q' || key == 'Q')
  {
    override = true;
    curr_time_override = "night";
  } else if (key == 'w' || key == 'W')
  {
    override = true;
    curr_time_override = "morning";
  } else if (key == 'e' || key == 'E')
  {
    override = true;
    curr_time_override = "day";
  } else if (key == 'r' || key == 'R')
  {
    override = true;
    curr_time_override = "sunset";
  } else if (key == 'h' || key == 'H')
  {
    documentation = !documentation;
  }
}


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
  public void trigger ()
  {
    hasTriggered = true;
  }
  public boolean checkFlag ()
  {
    return hasTriggered;
  }
}

static int increment = 180;//how many increments in a circle

class colorDisc 
{
  private ArrayList<alarm> alrms ; 

  private int col1;
  private int col2;
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
  colorDisc (int c1, int c2, int s, String type, int X, int Y)
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
  public int askCurrentColor ()
  {
    int from = col1;
    int to = col2;  

    int col = 150;

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

  public void giveAlarm(alarm a)
  {
    this.alrms.add (a);
  } 
  public void checkAlarms() 
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
  public void changecolours (int c1, int c2)
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
  public void updateTime()
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

  public float askRotation ()
  {
    return this.rotation;
  }

  public void render (int fc)
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

    int from = col1;
    int to = col2;    
    //calculate rotation increment amount
    float rotamt = map (1, 0, increment-1, 0, PI); // static 

    //in two loops for both halves
    for (int i = 0; i< increment; i++)
    {      
      float amt = map (i, 0, increment, 0, 1);     
      int curr = lerpColor(from, to, amt);
      stroke (curr);
      line(0, 0, size, 0);
      rotate (rotamt);
    }
    for (int i = 0; i< increment-1; i++)
    {      
      float amt = map (i, 0, increment, 0, 1);     
      int curr = lerpColor(to, from, amt);
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
      public void settings() {  size (500, 500);  smooth(); }
      static public void main(String[] passedArgs) {
            String[] appletArgs = new String[] { "final_1" };
            if (passedArgs != null) {
              PApplet.main(concat(appletArgs, passedArgs));
            } else {
              PApplet.main(appletArgs);
            }
      }
}
