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

import android.view.KeyEvent;

public class ProcessingAttVividF extends PApplet{

	int At_pr=0; int Med_pr=0; int S=0; int P=0;
	int Ra; int Ga; int Ba;
	int Rm; int Gm; int Bm;
	
	int pts = 10; 
	float angle = 0;
	float radiusAt = 30.0f; float radiusMed = 10.0f; float AtDynamicR = radiusAt;
	int acceleration = 1;

	// lathe segments
	int segments = 40;
	float latheAngle = 0;
	float latheRadiusAt = 60.0f; float latheRadiusMed = 50.0f;  float DynamicIterrN = radiusMed;

	//vertices
	PVector vertices[], vertices2[];

	// for shaded or wireframe rendering 
	boolean isWireFrame = false;

	// for optional helix
	boolean isHelix = false;
	float helixOffset = 5.0f;

	// -- sierpinski triangle
	PVector vert1, vert2, vert3;
	PVector[] coords1, coords2, coords3;
	int iterrationN = 0;
	
	// -- vivid fractal
	float frac = 24;	float slope = 0.8f; 	float sc = 1.8f;
	float nx,ny;	float TH = 0;	float depth = 1;	 
	PVector one,two,shift;
	
	int ir_t = 2000;  int ir_t1 = 50;
	
	public void setup(){	 
		 // frameRate(1);  // Animate slowly
		  background(0); 
		  noSmooth();
//		  smooth();
		 // noStroke();
		 // colorMode(HSB, 8, 100, 100);
		  
		  // -- sierpinski triangle
		  vert1 = new PVector(displayWidth/2 - 4*displayWidth/10, displayHeight/2 + 2*displayHeight/10);
		  vert2 = new PVector(displayWidth/2 + 4*displayWidth/10, displayHeight/2 + 2*displayHeight/10);
		  vert3 = new PVector (displayWidth/2, displayHeight/2 - 2*displayHeight/10);
		  
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
		  CalculateSierpTriangleItter();
		  //DynamicRadiusP();
		  		  
		  // -- blue(0)-violete(50)-red(100) 
		 /* Rm = (255 * Med_pr) / 100;
		  Gm = 0 ;
		  Bm = (255 * (100 - Med_pr)) / 100;
		  DynamicRadiusS();*/
		  
		  // - draw background and lighting setup
//		  background(0);  lights(); 
				 // directionalLight(mouseX/4, 0, mouseY/3, 1, 1, -1);
				 // ambientLight(0, 0, mouseY/5, 1, 1, -1);
		  
		  // -- center and spin toroid At (left)
		  pushMatrix();
		  translate(displayWidth/2, displayHeight - 9*displayHeight/10);
		  rotateZ(0);  rotateY(0);  rotateX(0);
		  thoroid(0,0, Ra, Ga, Ba, true, AtDynamicR, latheRadiusAt);
		  popMatrix();
		 
		  // -- center and spin toroid Med (right)
		/*  pushMatrix();
		  translate(displayWidth/2+350,displayHeight - 9*displayHeight/10);
		  rotateZ(0);  rotateY(0);	  rotateX(0);
		  thoroid(0,0, Rm, Gm, Bm, true, DynamicIterrN, latheRadiusMed);
		  popMatrix();*/
		 
		  // -- Draws the SierFractal2DColor (maximum 7 iteration visible)
//		  triangleSier(displayWidth/2 - 4*displayWidth/10, displayHeight/2 + 2*displayHeight/10,
//				  	   displayWidth/2 + 4*displayWidth/10, displayHeight/2 + 2*displayHeight/10,
//				  	   displayWidth/2, displayHeight/2 - 2*displayHeight/10,
//				  	   iterrationN);
		  
//		  int tShift = 15;
//		  triangleSier(displayWidth/2 - 4*displayWidth/10 + tShift*DynamicIterrN, displayHeight/2 + 2*displayHeight/10 + tShift*DynamicIterrN,
//			  	   displayWidth/2 + 4*displayWidth/10 + tShift*DynamicIterrN, displayHeight/2 + 2*displayHeight/10 + tShift*DynamicIterrN,
//			  	   displayWidth/2 + tShift*DynamicIterrN, displayHeight/2 - 2*displayHeight/10 + tShift*DynamicIterrN,
//			  	   iterrationN);
	  
		  // -- Vivid Fractal
		  one = new PVector(0,mouseX/2);  two = new PVector(width-0,mouseX/2);
		  noiseSeed(mouseY);
		 
//		  one = new PVector(0,ir_t/2);  two = new PVector(width-0,ir_t/2);
//		  noiseSeed(ir_t1);
		  ir_t1 = ir_t1 + 10;
		  
		  fill(0,5);  noStroke();  rect(0,0, width,height);
		 
		  tint(255,10);
		  image(g,random(-2,2),random(-2,2));
		  nx = noise(frameCount/1000.0f,0)*sc;
		  ny = noise(0,frameCount/1000.0f)*sc;
		   
		  stroke(255,10);
		 		 
		  for(int i= 0 ; i < frac;i++){
			    TH = frameCount/200.0f+i*(TWO_PI/frac);
			    pushMatrix();
			    translate(width/2,height/2);
			    rotate(-TH);
			    translate(-width/2,-height/2);
			 
			    fractal(one,two,depth);
			    popMatrix();
		  }
		    
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
	
	public void CalculateSierpTriangleItter(){
		 // -- limit number of iterations to 7			     
	    if (DynamicIterrN>=7)	{DynamicIterrN = 7; }          
	    if (DynamicIterrN<=1 )	{DynamicIterrN = 1; }
	     
	    float ClusterLeftX = 40; float ClusterRightX = 60;  float ClusterDelta = 0;
	    float graviton = 0.05f;
	    if(At_pr >= ClusterLeftX-ClusterDelta && At_pr <= ClusterRightX+ClusterDelta) 
	         				{ DynamicIterrN = DynamicIterrN; }
	         else {
	             if (At_pr > ClusterRightX+ClusterDelta )
	             			{ DynamicIterrN = DynamicIterrN + graviton;  } 
	             else {
	             	if (At_pr < ClusterLeftX-ClusterDelta )
	             			{ DynamicIterrN = DynamicIterrN - graviton;  }
	             }                    
	         }
	    iterrationN = (int)DynamicIterrN;
	     
	   // iterrationN = At_pr/10 - 3;
	    
	    if (iterrationN >=7)	{iterrationN  = 7; }          
	    if (iterrationN <=0 )	{iterrationN  = 0; }
	}
	
	public void triangleSier(float x1, float y1, float x2, float y2, float x3, float y3, int n) {
		  // 'n' is the number of iteration.
		  if ( n > 0 ) {
			stroke(255/n, 255/n, 0);
		    //fill(255/n, 0, 0);
		    fill(Ra, Ga, Ba);
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

	public void fractal(PVector p1,PVector p2,float depth){
		 
		  float d = dist(p1.x,p1.y,p2.x,p2.y);
		  if(d>=1.5f){
		    float theta = atan2(p2.y-p1.y,p2.x-p1.x)+(frameCount)/(((1/depth))*200.0f);
		 
		    PVector p3 =
		      new PVector(
		          (p1.x+p2.x)/2.0f+cos(theta)*nx*d/2.0f,
		          (p1.y+p2.y)/2.0f+sin(theta)*nx*d/2.0f);
		 
//		    stroke(lerpColor(0xffffccff,0xff00ccee,map(theta,-PI,PI,0,1)),10); //colors
		    stroke(lerpColor(0xffffccff,0xff00ccee,map(theta,-PI/2,PI/2,0,1)),10); //colors
		    if(d<50)
		      line(p1.x,p1.y,p2.x,p2.y);
		    depth *= slope;
		 
		    if(depth >= 0.1f){
		      fractal(p1,p3,depth);
		      fractal(p2,p3,depth);
		    }
		  }
		}
	
	public void keyPressed(){
	  
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	        return super.onKeyDown(keyCode, event);
	}
    @Override
    public void onPause() {        
        super.onPause();           
        //finish();
    }
	
	
	public int sketchWidth() { return displayWidth; }
	public int sketchHeight() { return displayHeight; }
	public String sketchRenderer() { return P3D; }

}

