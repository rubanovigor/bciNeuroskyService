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
	float accel_alpha = 0; float VerticalMovement = 0;
	float accel_rot = 0; float angleY = 0;
	
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
	boolean isWireFrame = true;

	// for optional helix
	boolean isHelix = false;
	float helixOffset = 5.0f;

	public void setup(){
	 
	  
	}

	public void draw(){
		At_pr = MainActivity.At;
		Med_pr = MainActivity.Med;
		S = At_pr - Med_pr;
		P = At_pr + Med_pr;
		 
		 // -- limit vertical acceleration
		 float Mconst = 0.7f;
	     if (accel_alpha>=Mconst)	{accel_alpha = Mconst;  }          
	     if (accel_alpha<=-Mconst)	{accel_alpha = -Mconst; }
	     
	     float ClusterLeftX = -30; float ClusterRightX = 30;  float ClusterDelta = 0;
	     float graviton = 0.10f;
	     if(S >= ClusterLeftX-ClusterDelta &&
	         	S <= ClusterRightX+ClusterDelta) 
	         				{ accel_alpha = accel_alpha - 0; VerticalMovement = VerticalMovement + accel_alpha; }
	         else {
	             if (S > ClusterRightX+ClusterDelta )
	             			{ accel_alpha = accel_alpha - graviton; VerticalMovement = VerticalMovement + accel_alpha; } 
	             else {
	             	if (S < ClusterLeftX-ClusterDelta )
	             			{ accel_alpha = accel_alpha + graviton; VerticalMovement = VerticalMovement + accel_alpha; }
	             }                    
	         }
	     
	     if ((displayHeight/2 + VerticalMovement) >= displayHeight) {VerticalMovement = -displayHeight/2;}
	     if ((displayHeight/2 + VerticalMovement) <= 0f) {VerticalMovement = displayHeight/2;}
	     
		
		
		
	  background(0);
	  // basic lighting setup
	  lights(); 
	 // directionalLight(mouseX/4, 0, mouseY/3, 1, 1, -1);
	 // ambientLight(0, 0, mouseY/5, 1, 1, -1);
	  
	  
	  // -- toroid1
	  	//center and spin toroid
	  	//translate(displayWidth/2-150,displayHeight/2, 0);
	  	rotateY(5f);
	  	rotateX(5f);
	  	thoroid(displayWidth/2-200,displayHeight/2 + VerticalMovement, false);
	  		
		//translate(0, 0, 0);
		//rotateX(frameCount*PI/200);
	  	//translate(mouseX, mouseY, 0);
	 
	  // -- toroid2
	  	// rotateZ(frameCount*PI/120);
	  	//rotateZ(frameCount*PI/10);
	  	//rotateY(frameCount*PI/170);
	  //rotateX(frameCount*PI/200);
	  	thoroid(displayWidth/2+200,displayHeight-150, true);
	  	
	  
	 
	  }


	public void thoroid (int _positionX, float verticalSpeed2, boolean isHelix_l) {
	    // 2 rendering styles
	  // wireframe or solid
	  if (isWireFrame){
	    stroke(0, _positionX/4, verticalSpeed2/3);
	    noFill();
	  } 
	  else {
	    noStroke();
	    fill(0, _positionX/4, verticalSpeed2/3);
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

	  // -- draw toroid
	  latheAngle = 0;
	  for(int i=0; i<=segments; i++){
	    beginShape(QUAD_STRIP);
	    for(int j=0; j<=pts; j++){
	      if (i>0){
	        vertex(vertices2[j].x, vertices2[j].y, vertices2[j].z);
	      }
	      vertices2[j].x = cos(radians(latheAngle))*vertices[j].x + PApplet.parseInt(_positionX);
	      vertices2[j].y = sin(radians(latheAngle))*vertices[j].x + PApplet.parseInt(verticalSpeed2);
	      vertices2[j].z = vertices[j].z;
	      // optional helix offset
	      if (isHelix){
	        vertices[j].z+=helixOffset;
	      } 
	      vertex(vertices2[j].x, vertices2[j].y, vertices2[j].z);
	    }
	    // -- create extra rotation for helix
	    if (isHelix_l){
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