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

	int pts = 10; 
	float angle = 0;
	float radius = 30.0f;
	int acceleration = 1;

	// lathe segments
	int segments = 60;
	float latheAngle = 0;
	float latheRadius = 100.0f;

	//vertices
	PVector vertices[], vertices2[];

	// for shaded or wireframe rendering 
	boolean isWireFrame = false;

	// for optional helix
	boolean isHelix = false;
	float helixOffset = 5.0f;

	public void setup(){
	 
	  
	}

	public void draw(){
	  background(0);
	  // basic lighting setup
	  lights(); 
	 // directionalLight(mouseX/4, 0, mouseY/3, 1, 1, -1);
	 // ambientLight(0, 0, mouseY/5, 1, 1, -1);
	  
	  //center and spin toroid
	 acceleration=acceleration-1;
	 
	  pushMatrix();
	  translate(width/2+150,height+acceleration);
	  rotateZ(acceleration*PI/100);
	  rotateY(acceleration*PI/160);
	  rotateX(acceleration*PI/120);

	  thoroid(0,0,mouseX/4, mouseY/3, mouseX-mouseY);
	 //translate(0, 0, 0);
	 //rotateX(frameCount*PI/200);
	 //translate(mouseX, mouseY, 0);
	   popMatrix();
	 
	 pushMatrix();
	 translate(width/2-150,height+acceleration);
	 rotateZ(acceleration*PI/200);
	 rotateY(acceleration*PI/200);
	 rotateX(acceleration*PI/120);
	 //println(frameCount);
	  thoroid(0,0, mouseY/3, mouseX/4, mouseY-480);
	  popMatrix();
	 
	  }


	public void thoroid (int _positionX, int _positionY, int _R, int _G, int _B) {
	    // 2 rendering styles
	  // wireframe or solid
	  if (isWireFrame){
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