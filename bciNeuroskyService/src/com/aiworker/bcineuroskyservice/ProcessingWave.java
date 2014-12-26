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
	// -- local EEG variables
	int pAt=0; int pMed=0; int pS=0; int pP=0;
	// -- variables to manipulate torroids colors dynamics
	int AtR; int AtG; int AtB; 	int MedR; int MedG; int MedB;
	
	// -- toroids setting
	int pts = 10; 	 int segments = 40;
	float angle = 0; float latheAngle = 0;
		// -- internal and external radius
	float radiusAt = 10.0f; float radiusMed = 10.0f;
	float latheRadiusAt = 50.0f; float latheRadiusMed = 50.0f; 
		// -- dynamic internal radius
	float AtDynamicR = radiusAt; float MedDynamicR = radiusMed;
		// -- for optional helix
	boolean isHelix = false;	float helixOffset = 5.0f;
		// -- toroids vertices
	PVector vertices[], vertices2[];

	
	// -- mainGUI (3 icons moving on the triangle)
	PVector vert1, vert2, vert3;
	PVector[] coords1, coords2, coords3, allCoords;
	float baseLength1, baseLength2, baseLength3, totalLength;
	int count1, count2, count3, countAll;
	float [] f1; 	float [] f2;	float [] f3; float [] fAllCoords;
		// -- speed of the moving icons in points
	float RotationSpeed = 5;
	
	// -- Sierpinski fractal iteration (from 0 to 7)
	int SierpF_iterN = 0;
	
	// --processing algorithm
	float TimeToSelectMax = 70; float TimeToSelect = TimeToSelectMax;  float TimeToSelectItt = 0.5f;
	float accel_alphaMax = 10; float accel_alpha = 0; float accel_alphaDeviation = 0.5f;
	boolean FirstRun = true; boolean action_cancel_flag = false;
	
	float AtDynamicAccDec = 0;
	

	
	public void setup(){	 
		 // frameRate(1);  // Animate slowly

		  smooth();
		 // noStroke();
		 // colorMode(HSB, 8, 100, 100);
		  
		  // -- setup vertices coordinates for triangle
		  vert1 = new PVector(displayWidth/2 - 4*displayWidth/10, displayHeight/2 + 2*displayHeight/10);
		  vert2 = new PVector(displayWidth/2 + 4*displayWidth/10, displayHeight/2 + 2*displayHeight/10);
		  vert3 = new PVector (displayWidth/2, displayHeight/2 - 2*displayHeight/10);

		  calculateTrianglePath();
		  count1 = 0; count2 = 0;  count3 = 0; countAll = 0;
		  f1 = coords1[count1].array(); 
		  f2 = coords2[count2].array();
		  f3 = coords3[count3].array();
		  fAllCoords = allCoords[countAll].array();
		  
	}

	public void draw(){
		  // -- draw background
		  background(0);
		  // -- basic lighting setup
		  lights(); 
				 // directionalLight(mouseX/4, 0, mouseY/3, 1, 1, -1);
				 // ambientLight(0, 0, mouseY/5, 1, 1, -1);
		  
		  getEEG();
		  
		  // -- At torroid	
		  		// -At- blue(0)-violete(50)-red(100) 
		  AtR = (255 * pAt) / 100;
		  AtG = 0;
		  AtB = (255 * (100 - pAt)) / 100 ;
		  		// -- create dynamic ts based on pS	
		  AtDynamicR = CreateDynamic(pP, AtDynamicR, radiusAt, latheRadiusAt, 0.5f, 85, 150, 0);
		  		// -- center and spin toroid At (left)
		  pushMatrix();
		  translate(displayWidth/2 - 3.5f*displayWidth/10, displayHeight - 9*displayHeight/10);
		  rotateZ(0);		  rotateY(0);		  rotateX(0);
		  thoroid(0,0, AtR, AtG, AtB, true, AtDynamicR, latheRadiusAt);
		  popMatrix();			  
		  
		  // -- Med torroid
		  		// --Med blue(0)-violete(50)-red(100) 
		  MedR = (255 * pMed) / 100;
		  MedG = 0 ;
		  MedB = (255 * (100 - pMed)) / 100;
		  		// -- create dynamic ts based on pS
		  MedDynamicR = CreateDynamic(pS, MedDynamicR, radiusMed, latheRadiusMed, 0.5f, -30, 30, 0);		 		 
		  		// -- center and spin toroid Med (right)
		  pushMatrix();
		  translate(displayWidth/2 + 3.5f*displayWidth/10,displayHeight - 9*displayHeight/10);
		  rotateZ(0);		  rotateY(0);		  rotateX(0);
		  thoroid(0,0, MedR, MedG, MedB, true, MedDynamicR, latheRadiusMed);
		  popMatrix();
		 
		  // -- Draws the SierFractal2DColor
//		  triangleSier(displayWidth/2 - 4*displayWidth/10, displayHeight/2 + 2*displayHeight/10,
//				  	   displayWidth/2 + 4*displayWidth/10, displayHeight/2 + 2*displayHeight/10,
//				  	   displayWidth/2, displayHeight/2 - 2*displayHeight/10,
//				  	   pAt/15);
		  
		  // -- Draws the SierFractal2DColor (maximum 7 iteration visible)
		  triangleSier(displayWidth/2 - 4*displayWidth/10, displayHeight/2 + 2*displayHeight/10,
				  	   displayWidth/2 + 4*displayWidth/10, displayHeight/2 + 2*displayHeight/10,
				  	   displayWidth/2, displayHeight/2 - 2*displayHeight/10,
				  	   SierpF_iterN);
		  
		  // -- setup coordinates for 3 icons		  
		  if (count1 >=totalLength ){   count1 = 0;   }
		  f3 = allCoords[count1].array();
		  
		  if (count2+baseLength1 >=totalLength ){   count2=(int) (-1*baseLength1);   }
		  f1 = allCoords[(int) (count2+baseLength1)].array(); 
		  
		  if (count3+2*baseLength1 >=totalLength ){   count3 = (int) (-2*baseLength1);   }
		  f2 = allCoords[(int) (count3+2*baseLength1)].array();
		  
		  // -- processing algorithm
		  ProcessingAlgorithm();
		  
		  
		  // -- speed of sphere movement	
		  // AttAccDeceleration();
		  count1=(int) (count1+RotationSpeed); 
		  count2=(int) (count2+RotationSpeed);  
		  count3=(int) (count3+RotationSpeed);
		  		//countAll = countAll + 3;
		    
		  // -- draw 3 icons
		  		// -- icon 1 (left-bottom vertices)
		  pushMatrix();	translate(f1[0], f1[1],0);
		  stroke(0,0,255);	fill (255,0,0);
		  sphere(50);		popMatrix();	   
		   		// -- icon 2  
		  pushMatrix();	translate(f2[0], f2[1],0);
		  stroke(0,0,255);	fill (0,255,0);
		  sphere(50);		popMatrix();		   
		   		// -- icon 3
		  pushMatrix();	translate(f3[0],f3[1],0);
		  stroke(0,255,0);	fill (0,0,255);
		  sphere(50);		popMatrix();
	  
	}


	public void thoroid (int _positionX, int _positionY, int _R, int _G, int _B, boolean isWireFrame_l,
			float radius_l, float latheRadius_l) {
	  // -- 2 rendering styles: wireframe or solid
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

	  // -- draw toroid
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
	      // -- optional helix offset
	      if (isHelix){
	        vertices[j].z+=helixOffset;
	      } 
	      vertex(vertices2[j].x, vertices2[j].y, vertices2[j].z);
	    }
	    // -- create extra rotation for helix
	    if (isHelix){
	      latheAngle+=720.0f/segments;
	    } 
	    else {
	      latheAngle+=360.0f/segments;
	    }
	    endShape();
	    
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

	/** calculate number of point inside vertices of the triangle and they coordinates	 */
	public void calculateTrianglePath() {
		  // -- calculate length (in points) of base top
		  baseLength1 = PVector.dist(vert1, vert2);
		  baseLength2 = PVector.dist(vert1, vert3);
		  baseLength3 = PVector.dist(vert2, vert3);
		  totalLength = baseLength1 + baseLength2 + baseLength3 ;

		  // fill base top coordinate array
		  int k = 0;
		  allCoords = new PVector[ceil(totalLength)];
		  coords1 = new PVector[ceil(baseLength3)];
		  for (int i=0; i<coords1.length; i++) {
		    coords1[i] = new PVector();
		    coords1[i].x = vert1.x + ((vert2.x-vert1.x)/baseLength1)*i;
		    coords1[i].y = vert1.y + ((vert2.y-vert1.y)/baseLength1)*i;
		    allCoords[k] = coords1[i];
		    k = k +1;
		    }
		  		  
		  coords3 = new PVector[ceil(baseLength3)];
		  for (int i=0; i<coords3.length; i++) {
		    coords3[i] = new PVector();
		    coords3[i].x = vert2.x + ((vert3.x-vert2.x)/baseLength3)*i;
		    coords3[i].y = vert2.y + ((vert3.y-vert2.y)/baseLength3)*i;
		    allCoords[k] = coords3[i];
		    k = k +1;
		  }
		  
		  coords2 = new PVector[ceil(baseLength3)];
		  for (int i=0; i<coords2.length; i++) {
		    coords2[i] = new PVector();
		    coords2[i].x = vert3.x + ((vert1.x-vert3.x)/baseLength2)*i;
		    coords2[i].y = vert3.y + ((vert1.y-vert3.y)/baseLength2)*i;
		    allCoords[k] = coords2[i];
		    k = k +1;
		  }
		  
		  totalLength = k-2;
		}
	/** get EEG data from MainActivity and calculate S,P */
	public void getEEG(){
		  pAt = MainActivity.At;
		  pMed = MainActivity.Med;
		  pS = pAt - pMed;
		  pP = pAt + pMed;
	}
	/** create dynamic time-series from rapid changing time-series 
	 * @return DynamicTS */
	public float CreateDynamic(int TS, float DynamicTS,
			float tsMin, float tsMax, float graviton,
			float ClusterX1, float ClusterX2, float ClusterDeltaX){
		 // -- limit time-series
	     if (DynamicTS>=tsMax)	{DynamicTS = tsMax; }          
	     if (DynamicTS<=tsMin )	{DynamicTS = tsMin; }
	     
	     // -- update DynamicTS depending on TS value
	     if(TS >= ClusterX1-ClusterDeltaX &&	TS <= ClusterX2+ClusterDeltaX) 
														   { DynamicTS = DynamicTS ; }
		 	else {
			 	if (TS > ClusterX2+ClusterDeltaX )		   { DynamicTS = DynamicTS + graviton; } 
				else {
						if (TS < ClusterX1-ClusterDeltaX ) { DynamicTS = DynamicTS - graviton;  }
					}                    
				}
	     
	     return DynamicTS;
	}
	/** Processing Algorithm of the user activity */
	public void ProcessingAlgorithm(){
	    // update RotationSpeed based on S
	    accel_alpha = StoDynamicMovement(pS, accel_alpha, 0, accel_alphaMax, 0.05f, -30, 30, 0);
	    RotationSpeed = accel_alpha/2;
	    
		// check if user want to send command
    		// -- check if rotational speed = 0 
	    if (accel_alpha<=0f && FirstRun!=true){
	    	action_cancel_flag = false;
	    	TimeToSelect = TimeToSelect - TimeToSelectItt;
	    	if (TimeToSelect<0f){ TimeToSelect = 0f;}
	    	SierpF_iterN = (int) (7 - TimeToSelect/10);
	    	}
	    else { // -- cancel command selection if rotational speed are not 0
	    	 if (TimeToSelect>0f && TimeToSelect<TimeToSelectMax && accel_alpha>0f){
	    		 action_cancel_flag = true;
	    		 TimeToSelect = TimeToSelectMax;
	    		 SierpF_iterN = 0;
	    		 FirstRun = true;
	    	 }
	    }
//	    if (TimeToSelect == TimeToSelectMax && accel_alpha>accel_alphaDeviation){ action_cancel_flag = false;}	
	    if (TimeToSelect == TimeToSelectMax && accel_alpha>accel_alphaDeviation){ FirstRun = false;}	
	    if (accel_alpha>accel_alphaDeviation){
   		 		 SierpF_iterN = 0; 
   		 		TimeToSelect = TimeToSelectMax;
   		 		 }
	   
	}
	
	public float StoDynamicMovement(int TS, float DynamicTS,
			float tsMin, float tsMax, float graviton,
			float ClusterX1, float ClusterX2, float ClusterDeltaX){
		 // -- limit time-series
	     if (DynamicTS>=tsMax)	{DynamicTS = tsMax; }          
	     if (DynamicTS<=tsMin )	{DynamicTS = tsMin; }
	     
	     // -- update DynamicTS depending on TS value
	     if(TS >= ClusterX1-ClusterDeltaX &&	TS <= ClusterX2+ClusterDeltaX)
	     												   { DynamicTS = DynamicTS - graviton; }
		 	else {
			 	if (TS > ClusterX2+ClusterDeltaX )		   { DynamicTS = DynamicTS + graviton; } 
				else {
						if (TS < ClusterX1-ClusterDeltaX ) { DynamicTS = DynamicTS + graviton; }
					}                    
				}
	     
	     if (DynamicTS>=tsMax)	{DynamicTS = tsMax; }          
	     if (DynamicTS<=tsMin )	{DynamicTS = tsMin; }
	     
	     return DynamicTS;
	}
	
	
	public void AttAccDeceleration(){
		 // -- limit radius
	     if (AtDynamicAccDec>=3)	{AtDynamicAccDec = 3; }          
	     if (AtDynamicAccDec<=0 )	{AtDynamicAccDec = 0; }
	     
	     float ClusterLeftY = 50; float ClusterRightY = 70;  float ClusterDeltaY = 0;
	     float graviton = 0.0025f;
	     if(pAt >= ClusterLeftY-ClusterDeltaY &&	pAt <= ClusterRightY+ClusterDeltaY) 
	         				{ AtDynamicAccDec = AtDynamicAccDec ; }
	         else {
	             if (pAt > ClusterRightY+ClusterDeltaY )
	             			{ AtDynamicAccDec = AtDynamicAccDec + graviton; } 
	             else {
	             	if (pAt < ClusterLeftY-ClusterDeltaY )
	             			{ AtDynamicAccDec = AtDynamicAccDec - graviton;  }
	             }                    
	         }
	     
	     RotationSpeed = RotationSpeed - AtDynamicAccDec;
	     if (RotationSpeed<=0) {SierpF_iterN = 1; RotationSpeed=0;}
	     if (RotationSpeed>=3) {SierpF_iterN = 0; RotationSpeed=3;}
	     
	}
	
	
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

	}
	
	public int sketchWidth() { return displayWidth; }
	public int sketchHeight() { return displayHeight; }
	public String sketchRenderer() { return P3D; }

}