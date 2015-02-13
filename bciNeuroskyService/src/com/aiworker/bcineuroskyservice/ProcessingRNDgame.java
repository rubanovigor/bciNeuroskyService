package com.aiworker.bcineuroskyservice;

import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.util.Random;
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

import android.view.KeyEvent;
/** activity for Network Game Sample        */
public class ProcessingRNDgame extends PApplet{
		// -- local EEG variables
		int pAt=0; int pMed=0; int pS=0; int pP=0;
		int index=0; int indexPl2=0;
			// -- variables to manipulate torroids colors dynamics
		int indexR; int indexG; int indexB; 
		int MedR; int MedG; int MedB;
		
		Random r = new Random(); private long mLastTime; int rndUpdDelay = 40;
		int GameLevel = 0; int MaxGameLevel=9; String rndMean = "mean";
		
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
	
		// -- Sierpinski fractal iteration (from 0 to 7)
		PVector vert1, vert2, vert3;
		int SierpF_iterN = 0; float DynamicIterrN=SierpF_iterN;
			// -- coordinates of triangle vertices
		float SVx1, SVy1, SVx2, SVy2, SVx3, SVy3;
		double alpha = Math.PI;

		
		// --processing algorithm
//		float TimeToSelectMax = 70; float TimeToSelect = TimeToSelectMax;  float TimeToSelectItt = 0.5f;
//		float accel_alphaMax = 10; float accel_alpha = 0; float accel_alphaDeviation = 0.5f;
//		boolean FirstRun = true; boolean action_cancel_flag = false;
		
		// -- network game play
		float Player2Accel = 0; float localPlayerAccel = 0;
		
		PFont f; 

		
		public void setup(){	 
			 // frameRate(1);  // Animate slowly

			  smooth();
			 // noStroke();
			 // colorMode(HSB, 8, 100, 100);
			  f = createFont("Arial",16,true); // STEP 3 Create Font
			  
			  // -- setup vertices coordinates for triangle
				SVx1 = displayWidth/2 + 0*displayWidth/10;
				SVy1 = displayHeight/2 + 3*displayHeight/10;
				SVx2 = displayWidth/2 + 4*displayWidth/10;
				SVy2 = displayHeight/2 + 3*displayHeight/10;
				SVx3 = displayWidth/2 + 2f*displayWidth/10;
				SVy3 = displayHeight/2 + 1*displayHeight/10;
				
//			  vert1 = new PVector(SVx1, SVy1);
//			  vert2 = new PVector(SVx2, SVy2);
//			  vert3 = new PVector(SVx3, SVy3);
		}

		public void draw(){
			  // -- draw background and setup basic lighting setup
			  background(0);  lights(); 
			  
			  // -- check game status
			  if (localPlayerAccel>Player2Accel && localPlayerAccel==(displayHeight - 1*displayHeight/10))
			  	{Player2Accel = 0; localPlayerAccel = 0; GameLevel = GameLevel + 1;} 
			  if (Player2Accel>localPlayerAccel && Player2Accel==(displayHeight - 1*displayHeight/10))
			  	{Player2Accel = 0; localPlayerAccel = 0; GameLevel = GameLevel - 1;} 
			  if (GameLevel<0)	{GameLevel = 0;} 
			  
			  // ===============================================
			  		// -- get EEG index (one from att/med/S/P)
			  index = getEEG();
			  
			  switch(MainActivity.toroidGameType){
				    case "you":
				    	displayLocalUserToroid(displayWidth/2,displayHeight - 1*displayHeight/10 - localPlayerAccel);
				    	break;
				    	
					case "rnd vs you":
						displayLocalUserToroid(displayWidth/2 + 2f*displayWidth/10,displayHeight - 1*displayHeight/10 - localPlayerAccel);
						displayPlayer2Toroid();
						displayGameLevel();
						break;
			  } 
			 
//			 // -- Draws the SierFractal2DColor (maximum 7 iteration visible)
//			  DynamicIterrN = Algorithm.CreateDynamic(pAt, DynamicIterrN, 0, 7, 0.01f, 30, 50, 0);
////			  DynamicIterrN = StoDynamicMovement(pAt, DynamicIterrN, 0, 7, 0.005f, 40, 60, 0);
//			  SierpF_iterN= (int)DynamicIterrN;
////			  SierpF_iterN=2;
//			  triangleSier(SVx1, SVy1- localPlayerAccel, SVx2, SVy2- localPlayerAccel, SVx3, SVy3- localPlayerAccel, SierpF_iterN);
////			  triangleSier((float)(SVx1*Math.sin(alpha/2)), (float)(SVy1*Math.sin(alpha))- localPlayerAccel,
////					  (float)(SVx2*Math.sin(alpha/4)), (float)(SVy2*Math.sin(alpha))- localPlayerAccel,
////					  (float)(SVx3*Math.sin(alpha/2)), (float)(SVy3*Math.sin(alpha))- localPlayerAccel,
////					  SierpF_iterN);
////			  alpha = alpha + Math.PI/100;
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
		/** calculate Sierpinski rtiangle depending on iteration */
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
		
		/** get EEG data from MainActivity and calculate S,P */
		public int getEEG(){			  
		 	 switch(MainActivity.UserControl){
		 	 case "att":
		 		 return eegService.At;
		 	 case "med":
		 		 return eegService.Med;
		 	 case "S":
		 		 return (eegService.At - eegService.Med);
		 	 case "P":
		 		 return (eegService.At + eegService.Med);
		 	 default:
		 	     return 0;
		 	 }
		}
			
		/** get random (Normal) distribution for userControl */
		public void getRndNormalDistribution(){			
			// -- create Randomly distributed time series
			  mLastTime = mLastTime +1;
			  if (mLastTime < rndUpdDelay) return; 
			  
			double val = 0;
			switch(MainActivity.UserControl){
			 case "att":
				 val = r.nextGaussian() * 25 + (50+GameLevel*5); // 50 (mean); 25 (standard deviation) - 70% of data
				 indexPl2  = (int) Math.round(val);
				 if (indexPl2 >100) {indexPl2 =100;} if (indexPl2 <0) {indexPl2 =0;}	           
				 if (mLastTime>rndUpdDelay){mLastTime=0;}
				 rndMean = "mean ~ " + String.valueOf(50+GameLevel*5);
			  	 break;
			 case "med":
				 val = r.nextGaussian() * 25 + (50+GameLevel*5); // 50 (mean); 25 (standard deviation) - 70% of data
				 indexPl2  = (int) Math.round(val);
				 if (indexPl2 >100) {indexPl2 =100;} if (indexPl2 <0) {indexPl2 =0;}	           
				 if (mLastTime>rndUpdDelay){mLastTime=0;}
				 rndMean = "mean ~ " + String.valueOf(50+GameLevel*5);
				 break;
			 case "S":
				 val = r.nextGaussian() * 25 + (50+GameLevel*5); // 50 (mean); 25 (standard deviation) - 70% of data
				 indexPl2  = (int) Math.round(val);
				 if (indexPl2 >100) {indexPl2 =100;} if (indexPl2 <0) {indexPl2 =0;}	
				 indexPl2 = indexPl2*2 - 100; // adjust to interval -100:100 
				 if (mLastTime>rndUpdDelay){mLastTime=0;}
				 rndMean = "mean ~ " + String.valueOf(0+GameLevel*5);
				 break;
			}
			
		}

		/** display toroid for local player */
		public void displayLocalUserToroid(float x, float y){
			  indexR = (255 * index) / 100; indexG = 0;  indexB = (255 * (100 - index)) / 100 ;
		  		// -- create dynamic ts based on index value
			  switch(MainActivity.UserControl){
				case "att":
				  localPlayerAccel = Algorithm.CreateDynamic(index, localPlayerAccel, 0, displayHeight - 1*displayHeight/10, 1.0f, 40, 60, 0);	
				  break;
				case "med":
				  localPlayerAccel = Algorithm.CreateDynamic(index, localPlayerAccel, 0, displayHeight - 1*displayHeight/10, 1.0f, 40, 60, 0);	
				  break;
				case "S":
				  localPlayerAccel = Algorithm.StoDynamicMovement(index, localPlayerAccel, 0, displayHeight - 1*displayHeight/10, 1.0f, -30, 30, 0);
				  break;
			  }
//			  localPlayerAccel = Algorithm.CreateDynamic(index, localPlayerAccel, 0, displayHeight - 1*displayHeight/10, 1.0f, 40, 60, 0);	
			  
		
			  		// -- center and spin toroid 	
			  pushMatrix();
//				  translate(displayWidth/2 + 2f*displayWidth/10,displayHeight - 1*displayHeight/10 - localPlayerAccel);
				  translate(x, y);
				  rotateZ(frameCount*PI/170); rotateY(frameCount*PI/170); rotateX(frameCount*PI/170);
				  thoroid(0,0, indexR, indexG, indexB, true, latheRadiusMed-10, latheRadiusMed);
			  popMatrix();
			  			
			  textFont(f,32);                 // STEP 4 Specify font to be used
			  fill(255);                        // STEP 5 Specify font color 
			  text(MainActivity.UserControl + " -> " + index+"\n    (you)",displayWidth/2 + 2.5f*displayWidth/10,displayHeight - 1.5f*displayHeight/10 - localPlayerAccel);  // STEP 6 Display Text
		}
		
		/** display toroid of the second/rnd player */
		public void displayPlayer2Toroid(){
			  getRndNormalDistribution();
			  // -- Player((random, network) (torroid on the left)
			  indexR = (255 * indexPl2 ) / 100; indexG = 0;  indexB = (255 * (100 - indexPl2 )) / 100 ;
			  		// -- create dynamic ts based on pS
			  switch(MainActivity.UserControl){
				case "att":
				  Player2Accel = Algorithm.CreateDynamic(indexPl2 , Player2Accel, 0, displayHeight - 1*displayHeight/10, 1.0f, 40, 60, 0);	
				  break;
				case "med":
				  Player2Accel = Algorithm.CreateDynamic(indexPl2 , Player2Accel, 0, displayHeight - 1*displayHeight/10, 1.0f, 40, 60, 0);	
				  break;
				case "S":
				  Player2Accel = Algorithm.StoDynamicMovement(indexPl2 , Player2Accel, 0, displayHeight - 1*displayHeight/10, 1.0f, -30, 30, 0);
				  break;
			  }
			  	// -- center and spin toroid 
			  pushMatrix();
			  translate(displayWidth/2 - 2f*displayWidth/10, displayHeight - 1*displayHeight/10 - Player2Accel);
			  //			  rotateZ(0);		  rotateY(0);		  rotateX(0);
			  rotateZ(frameCount*PI/170); rotateY(frameCount*PI/170); rotateX(frameCount*PI/170);
			  thoroid(0,0, indexR, indexG, indexB, true, latheRadiusMed-10, latheRadiusMed);
			  popMatrix();
			
			  textFont(f,32);         
			  fill(255);                   
			  text(MainActivity.UserControl + " -> " + indexPl2+"\n    ("+rndMean+")",displayWidth/2 - 1.5f*displayWidth/10,displayHeight - 1.5f*displayHeight/10 - Player2Accel);
			  
		}
		
		/** display game level for rnd vs you game */
		public void displayGameLevel(){
			  for(int i=0; i<MaxGameLevel; i++){
				  pushMatrix();
				  translate(1f*displayWidth/10 + i*displayWidth/10, displayHeight - 0.25f*displayHeight/10);
	//			  rotateZ(0);		  rotateY(0);		  rotateX(0);
				  thoroid(0,0, 172,172,172, false, (latheRadiusMed-10)/10, latheRadiusMed/10);
				  popMatrix();
			  }
			  
			  for(int i=0; i<GameLevel; i++){
//				  for(int i=0; i<4; i++){
				  pushMatrix();
				  translate(1f*displayWidth/10 + i*displayWidth/10, displayHeight - 0.25f*displayHeight/10);
//				  rotateZ(30*i);		  rotateY(20*i);		  rotateX(10*i);
				  thoroid(0,0, 0,255,0, false, (latheRadiusMed-10)/8, latheRadiusMed/8);
				  popMatrix();
			  }
		}
		
		public int sketchWidth() { return displayWidth; }
		public int sketchHeight() { return displayHeight; }
		public String sketchRenderer() { return P3D; }	
		
}