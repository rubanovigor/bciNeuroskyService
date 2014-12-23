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
	
	int pts = 10; 
	float angle = 0;
	float radiusAt = 10.0f; float radiusMed = 10.0f; float AtDynamicR = radiusAt;
	int acceleration = 1;

	// lathe segments
	int segments = 40;
	float latheAngle = 0;
	float latheRadiusAt = 50.0f; float latheRadiusMed = 50.0f;  float MedDynamicR = radiusMed;

	//vertices
	PVector vertices[], vertices2[];

	// for shaded or wireframe rendering 
	boolean isWireFrame = false;

	// for optional helix
	boolean isHelix = false;
	float helixOffset = 5.0f;

	KochFractal k;
	
	// -- mainGUI
	PVector vert1, vert2, vert3;
	PVector[] coords1, coords2, coords3;
	float baseLength1, baseLength2, baseLength3;
	int count1, count2, count3;
	float [] f1; 	float [] f2;	float [] f3;
	
	public void setup(){	 
		 // frameRate(1);  // Animate slowly
		  k = new KochFractal();
		  smooth();
		 // noStroke();
		 // colorMode(HSB, 8, 100, 100);
		  
		  // -- mainGUI
		  vert1 = new PVector(displayWidth/2 - 4*displayWidth/10, displayHeight/2 + 2*displayHeight/10);
		  vert2 = new PVector(displayWidth/2 + 4*displayWidth/10, displayHeight/2 + 2*displayHeight/10);
		  vert3 = new PVector (displayWidth/2, displayHeight/2 - 2*displayHeight/10);

		  createPath();
		  count1 = 0; count2 = 0;  count3 = 0;
		  f1 = coords1[count1].array(); 
		  f2 = coords2[count2].array();
		  f3 = coords3[count3].array();
		  
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
		  DynamicRadiusP();
		  		  
		  // -- blue(0)-violete(50)-red(100) 
		  Rm = (255 * Med_pr) / 100;
		  Gm = 0 ;
		  Bm = (255 * (100 - Med_pr)) / 100;
		  DynamicRadiusS();
		  
		  // - draw background
		  background(0);
		  		// basic lighting setup
		  lights(); 
				 // directionalLight(mouseX/4, 0, mouseY/3, 1, 1, -1);
				 // ambientLight(0, 0, mouseY/5, 1, 1, -1);
		  
		  // -- center and spin toroid At (left)
		  acceleration=acceleration-1;
		  pushMatrix();
		  		//translate(width/2+150,height+acceleration)
		  translate(displayWidth/2-350, displayHeight - 9*displayHeight/10);
		  rotateZ(0);
		  rotateY(0);
		  rotateX(0);
		  		//thoroid(0,0,mouseX/4, mouseY/3, mouseX-mouseY);
		  thoroid(0,0, Ra, Ga, Ba, true, AtDynamicR, latheRadiusAt);
				 //translate(0, 0, 0);
				 //rotateX(frameCount*PI/200);
				 //translate(mouseX, mouseY, 0);
		  popMatrix();
		 
		  // -- center and spin toroid Med (right)
		  pushMatrix();
		  		//translate(width/2-150,height+acceleration);
		  translate(displayWidth/2+350,displayHeight - 9*displayHeight/10);
		  		//rotateZ(acceleration*PI/120);
		  rotateZ(0);
		  rotateY(0);
		  rotateX(0);
		  		//println(frameCount);
		  		//thoroid(0,0, mouseY/3, mouseX/4, mouseY-480);
		  thoroid(0,0, Rm, Gm, Bm, true, MedDynamicR, latheRadiusMed);
		  popMatrix();
		 
		  // -- Draws the SierFractal2DColor
			  	//triangleSier(0, 700, 400, 0, 800, 700, mouseY/100);
			  	//triangleSier(0, 700, 400, 0, 800, 700, 4);
		  triangleSier(displayWidth/2 - 4*displayWidth/10, displayHeight/2 + 2*displayHeight/10,
				  	   displayWidth/2 + 4*displayWidth/10, displayHeight/2 + 2*displayHeight/10,
				  	   displayWidth/2, displayHeight/2 - 2*displayHeight/10,
				  	   At_pr/15);
		  
				  // -- Draws the snowflake!
		  			//pushMatrix();
				 /* k.render();
				  	// -- Iterate
				  k.nextLevel();
				  	// -- Let's not do it more than 5 times. . .
				  if (k.getCount() > 4) {
				    k.restart();
				  }*/
		  			//popMatrix();
	  
		  // -- Draw mainGUI
		  f1 = coords1[count1].array(); 
		  f2 = coords2[count2].array();
		  f3 = coords3[count3].array();  
		  
		  if (count3 >= (baseLength3 - 5) ){
			   count1 = 0; count2 = 0;  count3 = 0;
			   f1 = coords2[count2].array(); 
			   f2 = coords3[count3].array();
			   f3 = coords1[count1].array();
		  }
		  // -- speed of sphere movement
		  count1=count1+3;  count2=count2+3;  count3=count3+3;
		    
		 // println ("coord1 " + baseLength1);
		  // println ("coord2 " + baseLength2);
		  // println ("coord3 " + baseLength3);
		  
		  // -- point 1
		   pushMatrix();		   
		   translate(f1[0], f1[1],0);
		   stroke(0,0,255);
		   fill (255,0,0);
		   sphere(50);		   
		   popMatrix();
		   
		  // -- point 2  
		   pushMatrix();		     
		   translate(f2[0], f2[1],0);
		   stroke(0,0,255);
		   fill (0,255,0);
		   sphere(50);		   
		   popMatrix();
		   
		  // -- point 3
		   pushMatrix();
		   translate(f3[0],f3[1],0);
		   stroke(0,255,0);
		   fill (0,0,255);
		   sphere(50);
		   popMatrix();
	  
	}


	public void thoroid (int _positionX, int _positionY, int _R, int _G, int _B, boolean isWireFrame_l,
			float radius_l, float latheRadius_l) {
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
	    vertices[i].x = latheRadius_l + sin(radians(angle))*radius_l;
	    if (isHelix){
	      vertices[i].z = cos(radians(angle))*radius_l-(helixOffset* 
	        segments)/2;
	    } 
	    else{
	      vertices[i].z = cos(radians(angle))*radius_l;
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

	public void DynamicRadiusP(){
		 // -- limit radius
	     if (AtDynamicR>=latheRadiusAt)	{AtDynamicR = latheRadiusAt; }          
	     if (AtDynamicR<=radiusAt )		{AtDynamicR = radiusAt; }
	     
	     float ClusterLeftY = 85; float ClusterRightY = 150;  float ClusterDeltaY = 0;
	     float graviton = 0.5f;
	     if(P >= ClusterLeftY-ClusterDeltaY &&	P <= ClusterRightY+ClusterDeltaY) 
	         				{ AtDynamicR = AtDynamicR ; }
	         else {
	             if (P > ClusterRightY+ClusterDeltaY )
	             			{ AtDynamicR = AtDynamicR + graviton; } 
	             else {
	             	if (P < ClusterLeftY-ClusterDeltaY )
	             			{ AtDynamicR = AtDynamicR - graviton;  }
	             }                    
	         }
	     
	}
	
	public void DynamicRadiusS(){
		 // -- limit radius
	     if (MedDynamicR>=latheRadiusMed)	{MedDynamicR = latheRadiusMed; }          
	     if (MedDynamicR<=radiusMed )		{MedDynamicR = radiusMed; }
	     
	     float ClusterLeftX = -30; float ClusterRightX = 30;  float ClusterDelta = 0;
	     float graviton = 0.5f;
	     if(S >= ClusterLeftX-ClusterDelta &&
	         	S <= ClusterRightX+ClusterDelta) 
	         				{ MedDynamicR = MedDynamicR; }
	         else {
	             if (S > ClusterRightX+ClusterDelta )
	             			{ MedDynamicR = MedDynamicR + graviton;  } 
	             else {
	             	if (S < ClusterLeftX-ClusterDelta )
	             			{ MedDynamicR = MedDynamicR - graviton;  }
	             }                    
	         }
	     
	}
	
	public void triangleSier(float x1, float y1, float x2, float y2, float x3, float y3, int n) {
		  // 'n' is the number of iteration.
		  if ( n > 0 ) {
		    fill(255/n, 0, 0);
		    triangle(x1, y1, x2, y2, x3, y3);
		     
		    // Calculating the midpoints of all segments.
		    float h1 = (x1+x2)/2.0f;
		    float w1 = (y1+y2)/2.0f;
		    float h2 = (x2+x3)/2.0f;
		    float w2 = (y2+y3)/2.0f;
		    float h3 = (x3+x1)/2.0f;
		    float w3 = (y3+y1)/2.0f;
		     
		    // Trace the triangle with the new coordinates.
		    triangleSier(x1, y1, h1, w1, h3, w3, n-1);
		    triangleSier(h1, w1, x2, y2, h2, w2, n-1);
		    triangleSier(h3, w3, h2, w2, x3, y3, n-1);
		  }
		}

	public void createPath() {
		  // calculate length of base top
		  baseLength1 = PVector.dist(vert1, vert2);
		  baseLength2 = PVector.dist(vert1, vert3);
		  baseLength3 = PVector.dist(vert2, vert3);

		  // fill base top coordinate array
		  coords1 = new PVector[ceil(baseLength3)];
		  for (int i=0; i<coords1.length; i++) {
		    coords1[i] = new PVector();
		    coords1[i].x = vert1.x + ((vert2.x-vert1.x)/baseLength1)*i;
		    coords1[i].y = vert1.y + ((vert2.y-vert1.y)/baseLength1)*i;
		    }
		  
		  coords2 = new PVector[ceil(baseLength3)];
		  for (int i=0; i<coords2.length; i++) {
		    coords2[i] = new PVector();
		    coords2[i].x = vert3.x + ((vert1.x-vert3.x)/baseLength2)*i;
		    coords2[i].y = vert3.y + ((vert1.y-vert3.y)/baseLength2)*i;
		  }
		  
		  coords3 = new PVector[ceil(baseLength3)];
		  for (int i=0; i<coords3.length; i++) {
		    coords3[i] = new PVector();
		    coords3[i].x = vert2.x + ((vert3.x-vert2.x)/baseLength3)*i;
		    coords3[i].y = vert2.y + ((vert3.y-vert2.y)/baseLength3)*i;
		  }
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
//	  if (key =='a'){
//	    if (latheRadius>0){
//	      latheRadius--; 
//	    }
//	  } 
//	  else if (key == 's'){
//	    latheRadius++; 
//	  }
//	  // ellipse radius
//	  if (key =='z'){
//	    if (radius>10){
//	      radius--;
//	    }
//	  } 
//	  else if (key == 'x'){
//	    radius++;
//	  }
//	  // wireframe
//	  if (key =='w'){
//	    if (isWireFrame){
//	      isWireFrame=false;
//	    } 
//	    else {
//	      isWireFrame=true;
//	    }
//	  }
//	  // helix
//	  if (key =='h'){
//	    if (isHelix){
//	      isHelix=false;
//	    } 
//	    else {
//	      isHelix=true;
//	    }
//	  }
	}


	public int sketchWidth() { return displayWidth; }
	public int sketchHeight() { return displayHeight; }
	public String sketchRenderer() { return P3D; }

}