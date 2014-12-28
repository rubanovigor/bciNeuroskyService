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
public class ProcessingNewtorkGame extends PApplet{
		// -- local EEG variables
		int pAt=0; int pMed=0; int pS=0; int pP=0; int pAtRndNorm=0;
		// -- variables to manipulate torroids colors dynamics
		int AtR; int AtG; int AtB; 	int MedR; int MedG; int MedB;
		Random r = new Random(); private long mLastTime; int rndUpdDelay = 20;
		int GameLevel = 0;
		
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
	
		// --processing algorithm
//		float TimeToSelectMax = 70; float TimeToSelect = TimeToSelectMax;  float TimeToSelectItt = 0.5f;
//		float accel_alphaMax = 10; float accel_alpha = 0; float accel_alphaDeviation = 0.5f;
//		boolean FirstRun = true; boolean action_cancel_flag = false;
		
		float AtDynamicAccDec = 0;
		float Pl1Accel = 0; float Pl2Accel = 0;
		

		
		public void setup(){	 
			 // frameRate(1);  // Animate slowly

			  smooth();
			 // noStroke();
			 // colorMode(HSB, 8, 100, 100);
		  
		}

		public void draw(){
			  // -- draw background and setup basic lighting setup
			  background(0);  lights(); 
			  
			  // -- check game status
			  if (Pl2Accel>Pl1Accel && Pl2Accel==(displayHeight - 1*displayHeight/10))
			  	{Pl1Accel = 0; Pl2Accel = 0; GameLevel = GameLevel + 1;} 
			  if (Pl1Accel>Pl2Accel && Pl1Accel==(displayHeight - 1*displayHeight/10))
			  	{Pl1Accel = 0; Pl2Accel = 0; GameLevel = GameLevel - 1;} 
			  if (GameLevel<0)	{GameLevel = 0;} 
			  
			  for(int i=0; i<GameLevel; i++){
				  pushMatrix();
				  translate(1f*displayWidth/10 + i*displayWidth/10, displayHeight - 0.5f*displayHeight/10);
	//			  rotateZ(0);		  rotateY(0);		  rotateX(0);
				  thoroid(0,0, 0, 255, 0, false, (latheRadiusMed-10)/10, latheRadiusMed/10);
				  popMatrix();
			  }
			  
			  // -- get EEG data
			  getEEG();
			  			  
			  	
			  // -- At(random) Player1
//			  AtR = (255 * pAtRndNorm) / 100;  AtG = 0;  AtB = (255 * (100 - pAtRndNorm)) / 100 ;
			  		// -- center and spin toroid for At (left)
//			  pushMatrix();
//			  translate(displayWidth/2 - 4f*displayWidth/10, displayHeight - 9*displayHeight/10);
//			  rotateZ(0);		  rotateY(0);		  rotateX(0);
//			  thoroid(0,0, AtR, AtG, AtB, true, AtDynamicR, latheRadiusAt);
//			  popMatrix();			  
			  
			  // -- At Player2
//			  AtR = (255 * pAt) / 100;  AtG = 0;  AtB = (255 * (100 - pAt)) / 100 ;	 		 
//			  		// -- center and spin toroid Med (right)
//			  pushMatrix();
//			  translate(displayWidth/2 + 4f*displayWidth/10,displayHeight - 9*displayHeight/10);
//			  rotateZ(0);		  rotateY(0);		  rotateX(0);
//			  thoroid(0,0, AtR, AtG, AtB, true, MedDynamicR, latheRadiusMed);
//			  popMatrix();
			 		  
			  // ===============================================
			  // ===============================================
			  // -- Player1((random, network) torroid
			  AtR = (255 * pAtRndNorm) / 100; AtG = 0;  AtB = (255 * (100 - pAtRndNorm)) / 100 ;
			  		// -- create dynamic ts based on pS
			  Pl1Accel = CreateDynamic(pAtRndNorm, Pl1Accel, 0, displayHeight - 1*displayHeight/10, 1.0f, 40, 60, 0);		 		 
			  		// -- center and spin toroid Med (right)
			  pushMatrix();
			  translate(displayWidth/2 - 2f*displayWidth/10, displayHeight - 1*displayHeight/10 - Pl1Accel);
//			  rotateZ(0);		  rotateY(0);		  rotateX(0);
			  rotateZ(frameCount*PI/170); rotateY(frameCount*PI/170); rotateX(frameCount*PI/170);
			  thoroid(0,0, AtR, AtG, AtB, true, latheRadiusMed-10, latheRadiusMed);
			  popMatrix();
			  
			  // -- Player2 torroid
			  AtR = (255 * pAt) / 100; AtG = 0;  AtB = (255 * (100 - pAt)) / 100 ;
			  		// -- create dynamic ts based on pS
			  Pl2Accel = CreateDynamic(pAt, Pl2Accel, 0, displayHeight - 1*displayHeight/10, 1.0f, 40, 60, 0);	
			  		// -- center and spin toroid Med (right)	
			  pushMatrix();
			  translate(displayWidth/2 + 2f*displayWidth/10,displayHeight - 1*displayHeight/10 - Pl2Accel);
//			  rotateZ(0);		  rotateY(0);		  rotateX(0);
			  rotateZ(frameCount*PI/170); rotateY(frameCount*PI/170); rotateX(frameCount*PI/170);
			  thoroid(0,0, AtR, AtG, AtB, true, latheRadiusMed-10, latheRadiusMed);
			  popMatrix();
			 
		  
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
		
		/** get EEG data from MainActivity and calculate S,P */
		public void getEEG(){
			  pAt = MainActivity.At;
			  pMed = MainActivity.Med;
			  pS = pAt - pMed;
			  pP = pAt + pMed;
			   
			  
			  // -- create Randomly distributed At
				  mLastTime = mLastTime +1;
				  if (mLastTime < rndUpdDelay) return; 
				  double val = r.nextGaussian() * 25 + (50+GameLevel*5); // 50 (mean); 30 (standard deviation) - 70% of data

				 // double val = r.nextGaussian() * 25 + 100; // 50 (mean); 30 (standard deviation) - 70% of data
				  pAtRndNorm = (int) Math.round(val);
				  if (pAtRndNorm>100) {pAtRndNorm=100;} if (pAtRndNorm<0) {pAtRndNorm=0;}	           
				  if (mLastTime>rndUpdDelay){mLastTime=0;}
			  
			  // -- take EEG data directly from service wo interaction with activity
			  //pAt = eegService.At;
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
		     
			 // -- limit time-series
		     if (DynamicTS>=tsMax)	{DynamicTS = tsMax; }          
		     if (DynamicTS<=tsMin )	{DynamicTS = tsMin; }
		     
		     return DynamicTS;
		}
		/** convert S to dynamic movement*/
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