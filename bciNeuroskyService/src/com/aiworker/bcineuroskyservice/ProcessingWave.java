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
		// -- coordinates of triangle vertices
	float SVx1, SVy1, SVx2, SVy2, SVx3, SVy3;
//	float IL_SVx1, IL_SVy1, IL_SVx2, IL_SVy2, IL_SVx3, IL_SVy3;
//	float IL_SVx1_1, IL_SVy1_1, IL_SVx1_2, IL_SVy1_2, IL_SVx1_3, IL_SVy1_3;
	float d = 0;  int r = 500; int IL_r = r/2; int IL1_r = r/4;
	
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

		  smooth();
		 // noStroke();
		 // colorMode(HSB, 8, 100, 100);
		  SVx1 = displayWidth/2 - 3*displayWidth/10;		SVy1 = displayHeight/2 + 1.5f*displayHeight/10;
		  SVx2 = displayWidth/2 + 3*displayWidth/10;		SVy2 = displayHeight/2 + 1.5f*displayHeight/10;
		  SVx3 = displayWidth/2;							SVy3 = displayHeight/2 - 1.5f*displayHeight/10;
		  
		  d = displayWidth/10;
		  
		  // -- setup vertices coordinates for drawing icons
//		  vert1 = new PVector(displayWidth/2 - 4*displayWidth/10, displayHeight/2 + 2*displayHeight/10);
//		  vert2 = new PVector(displayWidth/2 + 4*displayWidth/10, displayHeight/2 + 2*displayHeight/10);
//		  vert3 = new PVector (displayWidth/2, displayHeight/2 - 2*displayHeight/10);
		  		  
//		  calculatePathOnTriangle();
		  
//		  count1 = 0; count2 = 0;  count3 = 0; countAll = 0;
//		  f1 = coords1[count1].array(); 
//		  f2 = coords2[count2].array();
//		  f3 = coords3[count3].array();
//		  fAllCoords = allCoords[countAll].array();
		  
		  // -- setup initial coordinates of the icons
//		  count1 = 0; count2 = count1 + trSideLength;  count3 = count2+ trSideLength; 
//		  count1 = trSideLength/2; count2 = count1 + trSideLength;  count3 = count2+ trSideLength; 
		  
		  // -- using Processing's loadImage method to import an image and store it in the variable imgBack
		  imgBack = loadImage("b5_1.png"); // imgBack = loadImage("b11_big.png");
		  		// -- icons
		  imgMusic = loadImage("icon_musicplayer.png");
				  imgMusicPlay = loadImage("icon_play_white.png");
				  imgMusicStop = loadImage("icon_stop_white.png");
				  imgMusicNext = loadImage("icon_next_white.png");
		  imgCamera = loadImage("icon_cam.png");
				  imgCameraPicture = loadImage("icon_picture.png");
				  imgCameraShare = loadImage("icon_video.png");
				  imgCameraPrtSc = loadImage("icon_printscreen.png");
		  imgTerminal = loadImage("icon_console.png");
				  imgTerminalA = loadImage("icon_a.png");
				  imgTerminalC = loadImage("icon_c.png");
				  imgTerminalT = loadImage("icon_t.png");
		  
	}

	public void draw(){
		  // -- draw background
		  // background(0);
		  // -- basic lighting setup
		  lights(); 
		  
		  getEEG();
		  
		  image(imgBack, 0, 0);
		  
		  // -- At torroid	
		  		// -At- blue(0)-violete(50)-red(100) 
		  AtR = (255 * pAt) / 100;
		  AtG = 0;
		  AtB = (255 * (100 - pAt)) / 100 ;
		  		// -- create dynamic ts based on pS	
		  AtDynamicR = Algorithm.CreateDynamic(pP, AtDynamicR, radiusAt, latheRadiusAt, 0.5f, 85, 150, 0);
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
		  MedDynamicR = Algorithm.CreateDynamic(pS, MedDynamicR, radiusMed, latheRadiusMed, 0.5f, -30, 30, 0);		 		 
		  		// -- center and spin toroid Med (right)
		  pushMatrix();
		  translate(displayWidth/2 + 3.5f*displayWidth/10,displayHeight - 9*displayHeight/10);
		  rotateZ(0);		  rotateY(0);		  rotateX(0);
		  thoroid(0,0, MedR, MedG, MedB, true, MedDynamicR, latheRadiusMed);
		  popMatrix();		  
		  
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

		  
//		  if (rotationAngleDemo>=360){mindOSlayer=2;}
		  
		  
//		  SierpF_iterN = (int) (7*rotationAngle/360-1) + 1;
//		  SierpF_iterN = 3;
		  		// -- main triangle
		  SVx1  = (float) (displayWidth/2 + r * Math.sin(Math.toRadians(rotationAngle)) );
		  SVy1  = (float) (displayHeight/2 + r * Math.cos(Math.toRadians(rotationAngle)) );
		  SVx2  = (float) (displayWidth/2 + r * Math.sin(Math.toRadians(rotationAngle+120)) );
		  SVy2  = (float) (displayHeight/2 + r * Math.cos(Math.toRadians(rotationAngle+120)) );
		  SVx3  = (float) (displayWidth/2 + r * Math.sin(Math.toRadians(rotationAngle+240)) );
		  SVy3  = (float) (displayHeight/2 + r * Math.cos(Math.toRadians(rotationAngle+240)) );
		  
		  		// -- icons of the first layer
		  iconFirstLayer = getIconsFirstLayerCoordinates(rotationAngle, r/2, iconW, iconH);

		  		// -- icons of the second layer
		  icon01SecondLayer = getIconsSecondLayerCoordinates(rotationAngle, r/4, iconW, iconH, iconFirstLayer[0], iconFirstLayer[1]);
		  icon23SecondLayer = getIconsSecondLayerCoordinates(rotationAngle, r/4, iconW, iconH, iconFirstLayer[2], iconFirstLayer[3]);
		  icon45SecondLayer = getIconsSecondLayerCoordinates(rotationAngle, r/4, iconW, iconH, iconFirstLayer[4], iconFirstLayer[5]);

		  // -- Draws the SierFractal2DColor (maximum 7 iteration visible)
		  		// -- main triangle
		  triangleSier(SVx1, SVy1, SVx2, SVy2, SVx3, SVy3, SierpF_iterN, 0,0,0);
//		  triangleSier(SVx1, SVy1, SVx2, SVy2, SVx3, SVy3, 3, 0,0,0);
		  
		  // -- draw icons
		  if(mindOSlayer==1){
			  if(SierpF_iterN>=1 && SierpF_iterN<6){
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
			  }
			  if(SierpF_iterN>=6){
			  		image(imgMusic, displayWidth/2-1.5f*iconW/2, displayHeight/2-1.5f*iconH/2, iconW*1.5f, iconH*1.5f);
					  image(imgMusicPlay, iconFirstLayer[0], iconFirstLayer[1], iconW, iconH);
					  image(imgMusicStop, iconFirstLayer[2], iconFirstLayer[3], iconW, iconH);
					  image(imgMusicNext, iconFirstLayer[4], iconFirstLayer[5], iconW, iconH);
			  }
		  }
		  
		  if(mindOSlayer==2){
			  if(SierpF_iterN>=1){
			  image(imgMusicPlay, iconFirstLayer[0], iconFirstLayer[1], iconW, iconH);
			  image(imgMusicStop, iconFirstLayer[2], iconFirstLayer[3], iconW, iconH);
			  image(imgMusicNext, iconFirstLayer[4], iconFirstLayer[5], iconW, iconH);
			  
			  }
		  }
		  
			
	  			// -- 3 small one located on Vertices of main triangle
//		  triangleSier(SVx1-d/2, SVy1+d/2, SVx1+d/2, SVy1+d/2, SVx1, SVy1-d/2, 1, 255,255,0);
//		  triangleSier(SVx2-d/2, SVy2+d/2, SVx2+d/2, SVy2+d/2, SVx2, SVy2-d/2, 1, 0,255,0);
//		  triangleSier(SVx3-d/2, SVy3+d/2, SVx3+d/2, SVy3+d/2, SVx3, SVy3-d/2, 1, 0,0,255);
		  	    	 
		  
		  // -- setup coordinates for 3 icons		  
//		  if (count1 >=totalLength ){   count1 = 0;   }
//		  f1 = allCoords[count1].array();
//		  
//		  if (count2 >=totalLength ){   count2=0;   }
//		  f2 = allCoords[(int) (count2)].array(); 
//	  
//		  if (count3 >=totalLength ){   count3 = 0; }
//		  f3 = allCoords[(int) (count3)].array();
//		  
		  // -- processing algorithm
//		  ProcessingAlgorithm();
		  
		  
		  // -- speed of sphere movement	
//		   //AttAccDeceleration();
//		  count1=(int) (count1+RotationSpeed); 
//		  count2=(int) (count2+RotationSpeed);  
//		  count3=(int) (count3+RotationSpeed);
		  		//countAll = countAll + 3;
		    
		  // -- draw 3 icons
		  		// -- icon 1 (left-bottom vertices)
//		  pushMatrix();	//	translate(f1[0], f1[1],0);
//		  translate(IL_SVx1, IL_SVy1);
//		  stroke(0,0,255);	fill (255,0,0);
//		  sphere(30);		popMatrix();	
		  
//		  pushMatrix();  translate(IL_SVx1, IL_SVy1);
////		  rotateZ(rotationAngle/4);		  rotateY(rotationAngle/4);		  rotateX(rotationAngle/4);
//		  thoroid(0,0, 255,255,255, true, 20,30);  popMatrix();
		  
//		  pushMatrix();  translate(IL_SVx1_1, IL_SVy1_1);
//		  thoroid(0,0, 255,255,255, true, 10,15);	  popMatrix();
//		  
//		  pushMatrix();  translate(IL_SVx1_2, IL_SVy1_2);
//		  thoroid(0,0, 255,255,255, true, 10,15);	  popMatrix();
//		  
//		  pushMatrix();  translate(IL_SVx1_3, IL_SVy1_3);
//		  thoroid(0,0, 255,255,255, true, 10,15);	  popMatrix();
		  
		  
		   		// -- icon 2  
//		  pushMatrix();	// translate(f2[0], f2[1],0);
//		  translate(IL_SVx2, IL_SVy2);
//		  stroke(0,0,255);	fill (0,255,0);
//		  sphere(50);		popMatrix();		   
		   		// -- icon 3
//		  pushMatrix();	//translate(f3[0],f3[1],0);
//		  translate(IL_SVx3, IL_SVy3);
//		  stroke(0,255,0);	fill (0,0,255);
//		  sphere(50);		popMatrix();
	  
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
							 int n, int cR, int cG, int cB) {
		  // 'n' is the number of iteration.
		  if ( n > 0 ) {
//		    fill(255/n, 0, 0);
//			stroke (255,255,255);
		    fill(cR, cG, cB);
		    triangle(x1, y1, x2, y2, x3, y3);
		     
		    // Calculating the midpoints of all segments.
		    float h1 = (x1+x2)/2.0f;
		    float w1 = (y1+y2)/2.0f;
		    float h2 = (x2+x3)/2.0f;
		    float w2 = (y2+y3)/2.0f;
		    float h3 = (x3+x1)/2.0f;
		    float w3 = (y3+y1)/2.0f;
		     
		    // Trace the triangle with the new coordinates.
//		    stroke (255,0,0);
		    triangleSier(x1, y1, h1, w1, h3, w3, n-1, cR,cG,cB);
//		    stroke (0,255,0);
		    triangleSier(h1, w1, x2, y2, h2, w2, n-1, cR,cG,cB);
//		    stroke (0,0,255);
		    triangleSier(h3, w3, h2, w2, x3, y3, n-1, cR,cG,cB);
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
	public float[] getIconsFirstLayerCoordinates(float alphaL, float radius, int iW, int iH){
		float[] iconsCoordinates = new float[6];
		iconsCoordinates[0]  = (float) (displayWidth/2  + radius * Math.sin(Math.toRadians(alphaL)) ) 	- iW/2;
		iconsCoordinates[1]  = (float) (displayHeight/2 + radius * Math.cos(Math.toRadians(alphaL)) ) 	- iH/2;
		iconsCoordinates[2]  = (float) (displayWidth/2  + radius * Math.sin(Math.toRadians(alphaL+120)) )- iW/2;
		iconsCoordinates[3]  = (float) (displayHeight/2 + radius * Math.cos(Math.toRadians(alphaL+120)) )- iH/2;
		iconsCoordinates[4]  = (float) (displayWidth/2  + radius * Math.sin(Math.toRadians(alphaL+240)) )- iW/2;
		iconsCoordinates[5]  = (float) (displayHeight/2 + radius * Math.cos(Math.toRadians(alphaL+240)) )- iH/2;
		  
		return iconsCoordinates;
	}
	
	/** get array of coordinates of the SECOND Layer icons*/
	public float[] getIconsSecondLayerCoordinates(float alphaL, float radius, int iW, int iH, float centerX, float centerY){
		float[] iconsCoordinates = new float[6];
		iconsCoordinates[0] = (float) (centerX+iW/2 + radius * Math.sin(Math.toRadians(alphaL)) ) - iW/4;
		iconsCoordinates[1] = (float) (centerY+iH/2 + radius * Math.cos(Math.toRadians(alphaL)) ) - iH/4;
		iconsCoordinates[2] = (float) (centerX+iW/2 + radius * Math.sin(Math.toRadians(alphaL+120)) ) - iW/4;
		iconsCoordinates[3] = (float) (centerY+iH/2 + radius * Math.cos(Math.toRadians(alphaL+120)) ) - iH/4;
		iconsCoordinates[4] = (float) (centerX+iW/2 + radius * Math.sin(Math.toRadians(alphaL+240)) ) - iW/4;
		iconsCoordinates[5] = (float) (centerY+iH/2 + radius * Math.cos(Math.toRadians(alphaL+240)) ) - iH/4;
		  
//		  icon01SecondLayer[0] = (float) (iconFirstLayer[0]+iconW/2 + (r/4) * Math.sin(Math.toRadians(rotationAngle)) ) - iconW/4;
//		  icon01SecondLayer[1] = (float) (iconFirstLayer[1]+iconH/2 + (r/4) * Math.cos(Math.toRadians(rotationAngle)) ) - iconH/4;
//		  icon01SecondLayer[2] = (float) (iconFirstLayer[0]+iconW/2 + (r/4) * Math.sin(Math.toRadians(rotationAngle+120)) ) - iconW/4;
//		  icon01SecondLayer[3] = (float) (iconFirstLayer[1]+iconH/2 + (r/4) * Math.cos(Math.toRadians(rotationAngle+120)) ) - iconH/4;
//		  icon01SecondLayer[4] = (float) (iconFirstLayer[0]+iconW/2 + (r/4) * Math.sin(Math.toRadians(rotationAngle+240)) ) - iconW/4;
//		  icon01SecondLayer[5] = (float) (iconFirstLayer[1]+iconH/2 + (r/4) * Math.cos(Math.toRadians(rotationAngle+240)) ) - iconH/4;
		
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