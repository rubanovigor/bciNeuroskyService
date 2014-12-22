package com.aiworker.bcineuroskyservice;

import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class ProcessingWave extends PApplet {

	/**
	 * Interactive Toroid
	 * by Ira Greenberg. 
	 * 
	 * Illustrates the geometric relationship between Toroid, Sphere, and Helix
	 * 3D primitives, as well as lathing principal.
	 * 
	 * Instructions: <br />
	 * UP arrow key pts++ <br />
	 * DOWN arrow key pts-- <br />
	 * LEFT arrow key segments-- <br />
	 * RIGHT arrow key segments++ <br />
	 * 'a' key toroid radius-- <br />
	 * 's' key toroid radius++ <br />
	 * 'z' key initial polygon radius-- <br />
	 * 'x' key initial polygon radius++ <br />
	 * 'w' key toggle wireframe/solid shading <br />
	 * 'h' key toggle sphere/helix <br />
	 */
	
	int At_pr=0; int Med_pr=0; int S=0; int P=0;
	int Ra; int Ga; int Ba;
	int Rm; int Gm; int Bm;
	float accel_alpha = 0; float CoordY = 0; float accel_rot = 0;
	
	int pts = 10; 
	float angle = 0;
	float radius = 40.0f;
	int acceleration = 1;

	// lathe segments
	int segments = 40;
	float latheAngle = 0;
	float latheRadius = 70.0f;

	//vertices
	PVector vertices[], vertices2[];

	// for shaded or wireframe rendering 
	boolean isWireFrame = false;

	// for optional helix
	boolean isHelix = false;
	float helixOffset = 5.0f;

	KochFractal k;
	
	public void setup(){	 
		 // frameRate(1);  // Animate slowly
		  k = new KochFractal();
		  
	}

	public void draw(){
		  // -- get Att and Med from MainActivity
		  At_pr = MainActivity.At;
		  Med_pr = MainActivity.Med;
		  S = At_pr - Med_pr;
		  P = At_pr + Med_pr;
		  
		  // -- blue(0)-violete(50)-red(100) 
		  Ra = (255 * At_pr) / 100;
		  Ga = 0;
		  Ba = (255 * (100 - At_pr)) / 100 ;
		  
		  // -- blue(0)-violete(50)-red(100) 
		  Rm = (255 * Med_pr) / 100;
		  Gm = 0 ;
		  Bm = (255 * (100 - Med_pr)) / 100;
		  
		  // - draw background
		  background(0);
		  		// basic lighting setup
		  lights(); 
				 // directionalLight(mouseX/4, 0, mouseY/3, 1, 1, -1);
				 // ambientLight(0, 0, mouseY/5, 1, 1, -1);
		  
		  // -- center and spin toroid left
		  acceleration=acceleration-1;
		  pushMatrix();
		  		//translate(width/2+150,height+acceleration)
		  translate(displayWidth/2-350, displayHeight - 9*displayHeight/10);
		  rotateZ(0);
		  rotateY(100*sin(PI*P));
		  rotateX(0);
		  		//thoroid(0,0,mouseX/4, mouseY/3, mouseX-mouseY);
		  thoroid(0,0, Ra, Ga, Ba, true);
				 //translate(0, 0, 0);
				 //rotateX(frameCount*PI/200);
				 //translate(mouseX, mouseY, 0);
		  popMatrix();
		 
		  // -- center and spin toroid right
		  pushMatrix();
		  		//translate(width/2-150,height+acceleration);
		  translate(displayWidth/2+350,displayHeight - 9*displayHeight/10);
//		  rotateZ(acceleration*PI/120);
		  rotateZ(0);
		  rotateY(0);
		  rotateX(10*sin(PI*S));
		  		//println(frameCount);
		  		//thoroid(0,0, mouseY/3, mouseX/4, mouseY-480);
		  thoroid(0,0, Rm, Gm, Bm, false);
		  popMatrix();
		 
		  // -- Draws the snowflake!
//		  pushMatrix();
		 /* k.render();
		  	// -- Iterate
		  k.nextLevel();
		  	// -- Let's not do it more than 5 times. . .
		  if (k.getCount() > 4) {
		    k.restart();
		  }*/
//		  popMatrix();
	  
	  
	}


	public void thoroid (int _positionX, int _positionY, int _R, int _G, int _B, boolean isWireFrame_l) {
	    // 2 rendering styles
	  // wireframe or solid
	  if (isWireFrame_l){
	    stroke(_R, _G, _B);
	    noFill();
	  } 
	  else {
	    noStroke();
	    fill(_R, _G, _B);
	  }

	  vertices = new PVector[pts+1];
	  vertices2 = new PVector[pts+1];
	  
	  // fill arrays
	  for(int i=0; i<=pts; i++){
	    vertices[i] = new PVector();
	    vertices2[i] = new PVector();
	    vertices[i].x = latheRadius + sin(radians(angle))*radius;
	    if (isHelix){
	      vertices[i].z = cos(radians(angle))*radius-(helixOffset* 
	        segments)/2;
	    } 
	    else{
	      vertices[i].z = cos(radians(angle))*radius;
	    }
	    angle+=360.0f/pts;
	  }

	  // draw toroid
	  latheAngle = 0;
	  for(int i=0; i<=segments; i++){
	    beginShape(QUAD_STRIP);
	    for(int j=0; j<=pts; j++){
	      if (i>0){
	        vertex(vertices2[j].x, vertices2[j].y, vertices2[j].z);
	      }
	      vertices2[j].x = cos(radians(latheAngle))*vertices[j].x + PApplet.parseInt(_positionX);
	      vertices2[j].y = sin(radians(latheAngle))*vertices[j].x + PApplet.parseInt(_positionY);
	      vertices2[j].z = vertices[j].z;
	      // optional helix offset
	      if (isHelix){
	        vertices[j].z+=helixOffset;
	      } 
	      vertex(vertices2[j].x, vertices2[j].y, vertices2[j].z);
	    }
	    // create extra rotation for helix
	    if (isHelix){
	      latheAngle+=720.0f/segments;
	    } 
	    else {
	      latheAngle+=360.0f/segments;
	    }
	    endShape();
	    
	  }
	  //rotateX(frameCount*PI/150);
	}
	
	
	// Koch Curve
	// A class to manage the list of line segments in the snowflake pattern

	class KochFractal {
	  PVector start;       // A PVector for the start
	  PVector end;         // A PVector for the end
	  ArrayList<KochLine> lines;   // A list to keep track of all the lines
	  int count;
	  
	  KochFractal() {
	    start = new PVector(0,height-20);
	    end = new PVector(width,height-20);
	    lines = new ArrayList<KochLine>();
	    restart();
	  }

	  public void nextLevel() {  
	    // For every line that is in the arraylist
	    // create 4 more lines in a new arraylist
	    lines = iterate(lines);
	    count++;
	  }

	  public void restart() { 
	    count = 0;      // Reset count
	    lines.clear();  // Empty the array list
	    lines.add(new KochLine(start,end));  // Add the initial line (from one end PVector to the other)
	  
	  }
	  
	  public int getCount() {
	    return count;
	  }
	  
	  // This is easy, just draw all the lines
	  public void render() {
	    for(KochLine l : lines) {
	      l.display();
	    }
	  }

	  // This is where the **MAGIC** happens
	  // Step 1: Create an empty arraylist
	  // Step 2: For every line currently in the arraylist
	  //   - calculate 4 line segments based on Koch algorithm
	  //   - add all 4 line segments into the new arraylist
	  // Step 3: Return the new arraylist and it becomes the list of line segments for the structure
	  
	  // As we do this over and over again, each line gets broken into 4 lines, which gets broken into 4 lines, and so on. . . 
	  public ArrayList iterate(ArrayList<KochLine> before) {
	    ArrayList now = new ArrayList<KochLine>();    // Create emtpy list
	    for(KochLine l : before) {
	      // Calculate 5 koch PVectors (done for us by the line object)
	      PVector a = l.start();                 
	      PVector b = l.kochleft();
	      PVector c = l.kochmiddle();
	      PVector d = l.kochright();
	      PVector e = l.end();
	      // Make line segments between all the PVectors and add them
	      now.add(new KochLine(a,b));
	      now.add(new KochLine(b,c));
	      now.add(new KochLine(c,d));
	      now.add(new KochLine(d,e));
	    }
	    return now;
	  }

	}
	// The Nature of Code
	// Daniel Shiffman
	// http://natureofcode.com

	// Koch Curve
	// A class to describe one line segment in the fractal
	// Includes methods to calculate midPVectors along the line according to the Koch algorithm

	class KochLine {

	  // Two PVectors,
	  // a is the "left" PVector and 
	  // b is the "right PVector
	  PVector a;
	  PVector b;

	  KochLine(PVector start, PVector end) {
	    a = start.get();
	    b = end.get();
	  }

	  public void display() {
	    stroke(255);
	    line(a.x, a.y, b.x, b.y);
	  }

	  public PVector start() {
	    return a.get();
	  }

	  public PVector end() {
	    return b.get();
	  }

	  // This is easy, just 1/3 of the way
	  public PVector kochleft() {
	    PVector v = PVector.sub(b, a);
	    v.div(3);
	    v.add(a);
	    return v;
	  }    

	  // More complicated, have to use a little trig to figure out where this PVector is!
	  public PVector kochmiddle() {
	    PVector v = PVector.sub(b, a);
	    v.div(3);
	    
	    PVector p = a.get();
	    p.add(v);
	    
	    v.rotate(-radians(60));
	    p.add(v);
	    
	    return p;
	  }    

	  // Easy, just 2/3 of the way
	  public PVector kochright() {
	    PVector v = PVector.sub(a, b);
	    v.div(3);
	    v.add(b);
	    return v;
	  }
	}

	public void scaleRotationAngle(){
		 // -- limit vertical acceleration
//	     float MconstRot = 0.006f;
//	     if (accel_rot>=MconstRot)	{accel_rot = MconstRot;  }          
//	     if (accel_rot<=-MconstRot)	{accel_rot = MconstRot; }
//	     
//	     float ClusterLeftY = 85; float ClusterRightY = 150;  float ClusterDeltaY = 0;
//	     float gravitonY = 0.1f;
//	     if(P >= ClusterLeftY-ClusterDeltaY &&
//	         	P <= ClusterRightY+ClusterDeltaY) 
//	         				{ accel_rot = accel_rot - 0; angleY = angleY + accel_rot; }
//	         else {
//	             if (P > ClusterRightY+ClusterDeltaY )
//	             			{ accel_rot = accel_rot - gravitonY; angleY = angleY + accel_rot; } 
//	             else {
//	             	if (P < ClusterLeftY-ClusterDeltaY )
//	             			{ accel_rot = accel_rot + gravitonY; angleY = angleY + accel_rot; }
//	             }                    
//	         }
	     
	}
	/*
	 left/right arrow keys control ellipse detail
	 up/down arrow keys control segment detail.
	 'a','s' keys control lathe radius
	 'z','x' keys control ellipse radius
	 'w' key toggles between wireframe and solid
	 'h' key toggles between toroid and helix
	 */
	public void keyPressed(){
	  if(key == CODED) { 
	    // pts
	    if (keyCode == UP) { 
	      if (pts<40){
	        pts++;
	      } 
	    } 
	    else if (keyCode == DOWN) { 
	      if (pts>3){
	        pts--;
	      }
	    } 
	    // extrusion length
	    if (keyCode == LEFT) { 
	      if (segments>3){
	        segments--; 
	      }
	    } 
	    else if (keyCode == RIGHT) { 
	      if (segments<80){
	        segments++; 
	      }
	    } 
	  }
	  // lathe radius
	  if (key =='a'){
	    if (latheRadius>0){
	      latheRadius--; 
	    }
	  } 
	  else if (key == 's'){
	    latheRadius++; 
	  }
	  // ellipse radius
	  if (key =='z'){
	    if (radius>10){
	      radius--;
	    }
	  } 
	  else if (key == 'x'){
	    radius++;
	  }
	  // wireframe
	  if (key =='w'){
	    if (isWireFrame){
	      isWireFrame=false;
	    } 
	    else {
	      isWireFrame=true;
	    }
	  }
	  // helix
	  if (key =='h'){
	    if (isHelix){
	      isHelix=false;
	    } 
	    else {
	      isHelix=true;
	    }
	  }
	}


	  public int sketchWidth() { return displayWidth; }
	  public int sketchHeight() { return displayHeight; }
	  public String sketchRenderer() { return P3D; }

}