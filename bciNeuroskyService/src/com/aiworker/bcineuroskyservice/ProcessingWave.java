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

import android.util.Log;

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
//	PVector[] coords1, coords2, coords3, allCoords;
//	float baseLength1, baseLength2, baseLength3, totalLength;
//	int count1, count2, count3, countAll;
//	float [] f1; 	float [] f2;	float [] f3; float [] fAllCoords;
		// -- speed of the moving icons in points
	float RotationSpeed = 5; int trSideLength = 1000;
	
	// -- Sierpinski fractal iteration (from 0 to 7)
//	PVector vert1, vert2, vert3;
	int SierpF_iterN = 1; float DynamicIterrN=SierpF_iterN;
	// -- array for coordinates of Main Sierpinski triangle vertices
	float [] SierpTrV = new float [6];
	float d = 0;  int r = 500; int rt = 0;
	
	// -- processing algorithm
	float TimeToSelectMax = 70; float TimeToSelect = TimeToSelectMax;  float TimeToSelectItt = 0.5f;
	float accelerationMax = 10; float acceleration = 0; float accelerationDeviation = 0.5f;
		// -- rotational angle and acceleration 
	float rotationAngle = 0; float rotationAccel = 0.5f;
	float rotationAngleDemo = 0; float demoTT = 5f; float demoTTd = 0.025f;
	
	boolean FirstRun = true; boolean action_cancel_flag = false;
	int mindOSlayer = 1;
	
	float AtDynamicAccDec = 0;
	
	// -- Setting up a background and icons images
	PImage imgBack, imgMusic, imgCamera, imgTerminal;
	PImage imgMusicPlay, imgMusicStop, imgMusicNext; 
	PImage imgTerminalA, imgTerminalC, imgTerminalT; 
	PImage imgCameraPicture, imgCameraShare, imgCameraPrtSc; 
	// -- array for coordinates of the fist layer icons
	float [] iconFirstLayer = new float [6];  	
	// -- arrays for coordinates of the second layer icons
	float [] icon01SecondLayer = new float [6];
	float [] icon23SecondLayer = new float [6];
	float [] icon45SecondLayer = new float [6];

	int iconW = 100, iconH = 100; 

	
	public void setup(){	 
//		  frameRate(15);  // Animate slowly
		noFill();
		  smooth();
		 // noStroke();
		 // colorMode(HSB, 8, 100, 100);
		  	 background(0,0,0); 
		  	 
		  SierpTrV[0] = displayWidth/2 - 3*displayWidth/10;		SierpTrV[1] = displayHeight/2 + 1.5f*displayHeight/10;
		  SierpTrV[2] = displayWidth/2 + 3*displayWidth/10;		SierpTrV[3] = displayHeight/2 + 1.5f*displayHeight/10;
		  SierpTrV[4] = displayWidth/2;							SierpTrV[5] = displayHeight/2 - 1.5f*displayHeight/10;
		  
		  d = displayWidth/10;
		  
		  // -- using Processing's loadImage method to import an image and store it in the variable imgBack
//		  imgBack = loadImage("b5_1.png"); 
//		  imgBack = loadImage("b11_big.png");
		  imgBack = loadImage("b3_big.png");
		  		// -- icons
//		  imgMusic = loadImage("icon_musicplayer.png");
//				  imgMusicPlay = loadImage("icon_play_white.png");
//				  imgMusicStop = loadImage("icon_stop_white.png");
//				  imgMusicNext = loadImage("icon_next_white.png");
//		  imgCamera = loadImage("icon_cam.png");
//				  imgCameraPicture = loadImage("icon_picture.png");
//				  imgCameraShare = loadImage("icon_video.png");
//				  imgCameraPrtSc = loadImage("icon_printscreen.png");
//		  imgTerminal = loadImage("icon_console.png");
//				  imgTerminalA = loadImage("icon_a.png");
//				  imgTerminalC = loadImage("icon_c.png");
//				  imgTerminalT = loadImage("icon_t.png");
				
	  		// -- icons Colors
		  imgMusic = loadImage("play_music.png");
//				  imgMusicPlay = loadImage("play.png");
//				  imgMusicStop = loadImage("stop.png");
//				  imgMusicNext = loadImage("next.png");
				  imgMusicPlay = loadImage("play_blue.png");
				  imgMusicStop = loadImage("stop_blue.png");
				  imgMusicNext = loadImage("next_blue.png");
		  imgCamera = loadImage("camera.png");
				  imgCameraPicture = loadImage("icon_picture.png");
				  imgCameraShare = loadImage("twitter.png");
				  imgCameraPrtSc = loadImage("facebook.png");
		  imgTerminal = loadImage("google_search.png");
				  imgTerminalA = loadImage("google.png");
				  imgTerminalC = loadImage("gmail.png");
				  imgTerminalT = loadImage("hangouts.png");
				  
//		  imgTerminal = loadImage("settings1.png");
//				  imgTerminalA = loadImage("lightbulb.png");
//				  imgTerminalC = loadImage("car.png"); 
//				  imgTerminalT = loadImage("settings.png");
		  
	}

	public void draw(){
		  // -- setup background color (when wo image)
//		  	 background(160,160,160); 
		  // -- basic lighting setup
		  lights();  noFill();
		  
		  // -- setup background image
//		  image(imgBack, 0, 0);
		  
		  // -- get EEG data from service/MainActivity
		  getEEG();	  
		  
		  // -- At torroid	
		  		// -At- blue(0)-violete(50)-red(100) 
		  AtR = (255 * pAt) / 100;
		  AtG = 0;
		  AtB = (255 * (100 - pAt)) / 100 ;
		  		// -- create dynamic ts based on pS	
		  AtDynamicR = Algorithm.CreateDynamic(pP, AtDynamicR, radiusAt, latheRadiusAt, 0.5f, 85, 150, 0);
		  		// -- center and spin toroid At (left)
		  pushMatrix(); strokeWeight(1);
		  translate(displayWidth/2 - 3.5f*displayWidth/10, displayHeight - 9*displayHeight/10);
		  rotateZ(0);		  rotateY(0);		  rotateX(0);
		  thoroid(0,0, AtR, AtG, AtB, true, AtDynamicR, latheRadiusAt);		  popMatrix();			  
		  
		  // -- Med torroid
		  		// --Med blue(0)-violete(50)-red(100) 
		  MedR = (255 * pMed) / 100;
		  MedG = 0 ;
		  MedB = (255 * (100 - pMed)) / 100;
		  		// -- create dynamic ts based on pS
		  MedDynamicR = Algorithm.CreateDynamic(pS, MedDynamicR, radiusMed, latheRadiusMed, 0.5f, -30, 30, 0);		 		 
		  		// -- center and spin toroid Med (right)
		  pushMatrix();   strokeWeight(1);
		  translate(displayWidth/2 + 3.5f*displayWidth/10,displayHeight - 9*displayHeight/10);
		  rotateZ(0);		  rotateY(0);		  rotateX(0);
		  thoroid(0,0, MedR, MedG, MedB, true, MedDynamicR, latheRadiusMed);   popMatrix();		  
		  
		  // -- calculate acceleration
//		  acceleration = Algorithm.StoDynamicMovement(pS, acceleration, 0, accelerationMax, 0.05f, -30, 30, 0);
//		  acceleration = acceleration + 0.01f;
		  // -- convert acceleration to angle
//		  rotationAngle = Algorithm.CircularMovement(acceleration, 0, accelerationMax);
		  rotationAngle = rotationAngle + rotationAccel;		  if (rotationAngle>=360){rotationAngle=0;}
		  
		  // -- for Demo
		  rotationAngleDemo = rotationAngleDemo + rotationAccel;
		  if (rotationAngleDemo>=190){rotationAccel = rotationAccel -0.001f;}
		  if (rotationAccel<=0){rotationAccel = 0;}
		 
		  if (rotationAccel>=0.4f){SierpF_iterN = 2;}
			  if (rotationAccel>=0.2f && rotationAccel<0.4f){SierpF_iterN = 3;}
			  if (rotationAccel>0.0f && rotationAccel<0.2f){SierpF_iterN = 3;}
			  
			  
		  if (rotationAccel==0){
			  demoTT = demoTT - demoTTd;
			  if (demoTT<=0){demoTT = 0;}
			  if (demoTT>=4f && demoTT<4.5f){SierpF_iterN = 4;}
			  if (demoTT>=3f && demoTT<4f){SierpF_iterN = 5;}
			  if (demoTT>=2f && demoTT<3f){SierpF_iterN = 6;}
			  if (demoTT>=1f && demoTT<2f){SierpF_iterN = 7;}
			  if (demoTT>=0f && demoTT<1f){SierpF_iterN = 8; mindOSlayer=2; rotationAccel = 0.5f; demoTT=5f;}

		  }

		   		// -- main Sierpinski triangle
		  SierpTrV = getTriangleVertCoord(rotationAngle, displayWidth/2, displayHeight/2, r, 0, 0);
		  
		  		// -- icons of the first layer
		  iconFirstLayer = getTriangleVertCoord(rotationAngle, displayWidth/2, displayHeight/2, r/2, iconW/2, iconH/2);

		  		// -- icons of the second layer // centerX+iW/2
		  icon01SecondLayer = getTriangleVertCoord(rotationAngle, iconFirstLayer[0]+iconW/2, iconFirstLayer[1]+iconH/2, r/4, iconW/4, iconH/4);
		  icon23SecondLayer = getTriangleVertCoord(rotationAngle, iconFirstLayer[2]+iconW/2, iconFirstLayer[3]+iconH/2, r/4, iconW/4, iconH/4);
		  icon45SecondLayer = getTriangleVertCoord(rotationAngle, iconFirstLayer[4]+iconW/2, iconFirstLayer[5]+iconH/2, r/4, iconW/4, iconH/4);
		  
	  
		  // -- draw user interface (SierpTriangle, Icons, Animations)
		  if(mindOSlayer==1){
			  if(SierpF_iterN>=1 && SierpF_iterN<7){
				  triangleSier(SierpTrV[0],SierpTrV[1],SierpTrV[2],SierpTrV[3],SierpTrV[4],SierpTrV[5],
						  SierpF_iterN, 0,0,0,false, 0,127,255,10);
				  image(imgMusic, iconFirstLayer[0], iconFirstLayer[1], iconW, iconH);
				  image(imgCamera, iconFirstLayer[2], iconFirstLayer[3], iconW, iconH);
				  image(imgTerminal, iconFirstLayer[4], iconFirstLayer[5], iconW, iconH);
			  
				  	if(SierpF_iterN>=3){		
					  image(imgMusicPlay, icon01SecondLayer[0], icon01SecondLayer[1], iconW/2, iconH/2);
					  image(imgMusicStop, icon01SecondLayer[2], icon01SecondLayer[3], iconW/2, iconH/2); 
					  image(imgMusicNext, icon01SecondLayer[4], icon01SecondLayer[5], iconW/2, iconH/2);
					  
					  image(imgCameraPicture, icon23SecondLayer[0], icon23SecondLayer[1], iconW/2, iconH/2);
					  image(imgCameraShare, icon23SecondLayer[2], icon23SecondLayer[3], iconW/2, iconH/2); 
					  image(imgCameraPrtSc, icon23SecondLayer[4], icon23SecondLayer[5], iconW/2, iconH/2);
					  
					  image(imgTerminalA, icon45SecondLayer[0], icon45SecondLayer[1], iconW/2, iconH/2);
					  image(imgTerminalC, icon45SecondLayer[2], icon45SecondLayer[3], iconW/2, iconH/2); 
					  image(imgTerminalT, icon45SecondLayer[4], icon45SecondLayer[5], iconW/2, iconH/2);
					  
				  	}
				  	if(SierpF_iterN>=4 && SierpF_iterN<=6 ){
					   // -- draw SierpFractal in different color to highlight selected item
				  		int r_temp =500;
					  	SierpTrV = getTriangleVertCoord(rotationAngle, iconFirstLayer[0]+iconW/2, iconFirstLayer[1]+iconH/2, r_temp/2, 0, 0);
					  	triangleSier(SierpTrV[0],SierpTrV[1],SierpTrV[2],SierpTrV[3],SierpTrV[4],SierpTrV[5],
							   SierpF_iterN-1, 0,0,0,true, 192,0,0,10);
				  	}
			  }
			  if(SierpF_iterN>=7){
				  	// -- decrease radius of active triangle (fade out)
					  r = r - 30;  if (r<=0){r = 0;}
					  SierpTrV = getTriangleVertCoord(rotationAngle, displayWidth/2, displayHeight/2, r, 0, 0);
					  triangleSier(SierpTrV[0],SierpTrV[1],SierpTrV[2],SierpTrV[3],SierpTrV[4],SierpTrV[5],
							  SierpF_iterN, 47,47,79,false, 0,127,255,r/100);
					  
					  rt = rt + 30;  if (rt>=500){rt = 500;}
					  // -- zoom in fractal
					  SierpTrV = getTriangleVertCoord(rotationAngle, displayWidth/2, displayHeight/2, rt, 0, 0);
					  triangleSier(SierpTrV[0],SierpTrV[1],SierpTrV[2],SierpTrV[3],SierpTrV[4],SierpTrV[5],
							  3, 0,0,0,false, 0,127,255,10);
					  
					  if (rt==500){
//						  background(0,0,0); 
//				  	  image(imgMusic, displayWidth/2-1.5f*iconW/2, displayHeight/2-1.5f*iconH/2, iconW*1.5f, iconH*1.5f);
				      iconFirstLayer = getTriangleVertCoord(rotationAngle, displayWidth/2, displayHeight/2, rt/2, iconW/2, iconH/2);
					   image(imgMusicPlay, iconFirstLayer[0], iconFirstLayer[1], iconW, iconH);
					   image(imgMusicStop, iconFirstLayer[2], iconFirstLayer[3], iconW, iconH);
					   image(imgMusicNext, iconFirstLayer[4], iconFirstLayer[5], iconW, iconH);
					  }
			  }
		  }
		  
		  if(mindOSlayer==2){
			  r=500;
			  if(SierpF_iterN>=1){
				  triangleSier(SierpTrV[0],SierpTrV[1],SierpTrV[2],SierpTrV[3],SierpTrV[4],SierpTrV[5],
						  SierpF_iterN, 0,0,0,false, 0,127,255,10);
				  image(imgMusicPlay, iconFirstLayer[0], iconFirstLayer[1], iconW, iconH);
				  image(imgMusicStop, iconFirstLayer[2], iconFirstLayer[3], iconW, iconH);
				  image(imgMusicNext, iconFirstLayer[4], iconFirstLayer[5], iconW, iconH);
			  
			  }
		  }
		  
		  	  
		  // -- processing algorithm
//		  ProcessingAlgorithm();
		  	  
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
	/** build and draw Sierpinski fractals based on provided SierpF_iterN */
	public void triangleSier(float x1, float y1, float x2, float y2, float x3, float y3,
							 int n, int fR, int fG, int fB, boolean flagTransparent,
							 int sR, int sG, int sB, int sW) {
		  // 'n' is the number of iteration.
		  if ( n > 0 ) {
//		    fill(255/n, 0, 0); // fill(0, 0, 0); 
			if (flagTransparent){ noFill();} else {fill(fR, fG, fB);}
			stroke(sR, sG, sB);
		    strokeWeight(sW); // strokeCap(ROUND);
		    triangle(x1, y1, x2, y2, x3, y3);

		    // Calculating the midpoints of all segments.
		    float h1 = (x1+x2)/2.0f;
		    float w1 = (y1+y2)/2.0f;
		    float h2 = (x2+x3)/2.0f;
		    float w2 = (y2+y3)/2.0f;
		    float h3 = (x3+x1)/2.0f;
		    float w3 = (y3+y1)/2.0f;
		     
		    // Trace the triangle with the new coordinates.
//		    fill(255, 0, 0);
//		    stroke (0, 40, 217);
		    triangleSier(x1, y1, h1, w1, h3, w3, n-1, fR,fG,fB,flagTransparent, sR,sG,sB,sW);
//		    noFill();
//		    stroke (0, 60, 217);
		    triangleSier(h1, w1, x2, y2, h2, w2, n-1, fR,fG,fB,flagTransparent, sR,sG,sB,sW);
//		    noFill();
//		    stroke (0, 80, 217);
		    triangleSier(h3, w3, h2, w2, x3, y3, n-1, fR,fG,fB,flagTransparent, sR,sG,sB,sW);
		    
		  }
		}

	/** calculate number of point inside vertices of the triangle and they coordinates	 */
	public void calculatePathOnTriangle() {
//		  // -- calculate length (in points) between vertices
////		  baseLength1 = PVector.dist(vert1, vert2);
////		  baseLength2 = PVector.dist(vert1, vert3) - 1;
////		  baseLength3 = PVector.dist(vert2, vert3) - 2;
////		  totalLength = baseLength1 + (baseLength2) + (baseLength3) ;
////
////		  Log.d(getString(R.string.app_name), "ir_d calculatePathOnTriangle()"+baseLength1);
////		  Log.d(getString(R.string.app_name), "ir_d calculatePathOnTriangle()"+baseLength2);
////		  Log.d(getString(R.string.app_name), "ir_d calculatePathOnTriangle()"+baseLength3);
////		  Log.d(getString(R.string.app_name), "ir_d calculatePathOnTriangle()"+totalLength);
//		  
//		  // fill base top coordinate array
//		  totalLength = trSideLength + (trSideLength-1) + (trSideLength-2);
//		  Log.d(getString(R.string.app_name), "ir_d calculatePathOnTriangle()"+totalLength);
//		  int k = 0;
//		  allCoords = new PVector[ceil(totalLength)];
//		  
//		  coords1 = new PVector[ceil(trSideLength)];
//		  for (int i=0; i<coords1.length; i++) {
//		    coords1[i] = new PVector();
//		    coords1[i].x = vert1.x + i*(vert2.x-vert1.x)/trSideLength;
//		    coords1[i].y = vert1.y + i*(vert2.y-vert1.y)/trSideLength; // -- 0 for horizontal side
//		    // --
//		    allCoords[k] = new PVector();
//		    allCoords[k] = coords1[i];
//		    k = k +1;
//		    }
//		  
//		  coords2 = new PVector[ceil(trSideLength-1)];
//		  for (int i=0; i<coords2.length; i++) {
//		    coords2[i] = new PVector();
//		    coords2[i].x = vert2.x - (i+1)*(vert2.x-vert3.x)/trSideLength;
//		    coords2[i].y = vert2.y + (i+1)*(vert3.y-vert2.y)/trSideLength;
//		    // --
//		    allCoords[k] = new PVector();
//		    allCoords[k] = coords2[i];
//		    k = k +1;
//		  }
//		  		  
//		  coords3 = new PVector[ceil(trSideLength-2)];
//		  for (int i=0; i<coords3.length; i++) {
//		    coords3[i] = new PVector();
//		    coords3[i].x = vert3.x - (i+1)*(vert3.x-vert1.x)/trSideLength;
//		    coords3[i].y = vert3.y - (i+1)*(vert3.y-vert1.y)/trSideLength;
//		    
//		    // --
//		    allCoords[k] = new PVector();
//		    allCoords[k] = coords3[i];
//		    k = k + 1;
//		  }
//		 		  
////		  totalLength = k-1;
		}
	
	/** get EEG data from MainActivity and calculate S,P */
	public void getEEG(){
		  pAt = MainActivity.At;
		  pMed = MainActivity.Med;
		  pS = pAt - pMed;
		  pP = pAt + pMed;
		  
		  //pAt = eegService.At;
	}
	
	/** get array of coordinates of the FIRST Layer icons*/
	public float[] getTriangleVertCoord(float alphaL, float centerX, float centerY, float radius, int iW, int iH){
		float[] iconsCoordinates = new float[6];
		iconsCoordinates[0]  = (float) (centerX + radius * Math.sin(Math.toRadians(alphaL)) )		- iW;
		iconsCoordinates[1]  = (float) (centerY + radius * Math.cos(Math.toRadians(alphaL)) )		- iH;
		iconsCoordinates[2]  = (float) (centerX + radius * Math.sin(Math.toRadians(alphaL+120)) )	- iW;
		iconsCoordinates[3]  = (float) (centerY + radius * Math.cos(Math.toRadians(alphaL+120)) )	- iH;
		iconsCoordinates[4]  = (float) (centerX + radius * Math.sin(Math.toRadians(alphaL+240)) )	- iW;
		iconsCoordinates[5]  = (float) (centerY + radius * Math.cos(Math.toRadians(alphaL+240)) )	- iH;
		
		return iconsCoordinates;
	}
	
	
	
	/** Processing Algorithm of the user activity */
	public void ProcessingAlgorithm(){
	    // update RotationSpeed based on S
	    acceleration = Algorithm.StoDynamicMovement(pS, acceleration, 0, accelerationMax, 0.05f, -30, 30, 0);
	    RotationSpeed = acceleration/2;
	    
		// check if user want to send command
    		// -- check if rotational speed = 0 
	    if (acceleration<=0f && FirstRun!=true){
	    	action_cancel_flag = false;
	    	TimeToSelect = TimeToSelect - TimeToSelectItt;
	    	if (TimeToSelect<0f){ TimeToSelect = 0f;}
	    	SierpF_iterN = (int) (7 - TimeToSelect/10);
	    	}
	    else { // -- cancel command selection if rotational speed are not 0
	    	 if (TimeToSelect>0f && TimeToSelect<TimeToSelectMax && acceleration>0f){
	    		 action_cancel_flag = true;
	    		 TimeToSelect = TimeToSelectMax;
	    		 SierpF_iterN = 0;
	    		 FirstRun = true;
	    	 }
	    }
//	    if (TimeToSelect == TimeToSelectMax && acceleration>accelerationDeviation){ action_cancel_flag = false;}	
	    if (TimeToSelect == TimeToSelectMax && acceleration>accelerationDeviation){ FirstRun = false;}	
	    if (acceleration>accelerationDeviation){
   		 		 SierpF_iterN = 0; 
   		 		 TimeToSelect = TimeToSelectMax;
   		 	}
	   
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