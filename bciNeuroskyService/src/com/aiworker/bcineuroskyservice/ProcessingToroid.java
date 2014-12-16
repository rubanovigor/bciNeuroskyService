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

public class ProcessingToroid extends PApplet{

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
int pts = 40; 
float angle = 0;
float radius = 60.0f;

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
	 At_pr = MainActivity.At;
	 Med_pr = MainActivity.Med;
	 S = At_pr- Med_pr;
	 P = At_pr + Med_pr;
	 
  //background(50, 64, 42);
  background(0, 0, 0);
  // basic lighting setup
  lights();
  // 2 rendering styles
  // wireframe or solid
  if (isWireFrame){
    //stroke(255, 255, 150);
    stroke(179, 79, 240);
    noFill();
  } 
  else {
    noStroke();
   // fill(150, 195, 125);
    stroke(159, 79, 240);
  }
  //center and spin toroid
  //translate(mouseX/2, mouseY/2, -100);
  //translate(At_pr, Med_pr, -100);
  translate(300, S*5+300, -100);

  //rotateX(frameCount*PI/150);
  //rotateY(frameCount*PI/170);
  //rotateZ(frameCount*PI/90);
 // rotateX(At_pr);
  //rotateY(Med_pr);
  rotateX(0);
  rotateY(P/2);
  rotateZ(0);

  // initialize point arrays
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
      vertices2[j].x = cos(radians(latheAngle))*vertices[j].x;
      vertices2[j].y = sin(radians(latheAngle))*vertices[j].x;
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


  public int sketchWidth() { return 800; }
  public int sketchHeight() { return 600; }
  public String sketchRenderer() { return P3D; }
  
}
