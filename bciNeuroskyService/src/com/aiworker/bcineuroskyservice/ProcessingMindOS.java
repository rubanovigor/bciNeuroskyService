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

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

public class ProcessingMindOS extends PApplet {
	
	// -- local EEG variables
	int pAt=0; int pMed=0; int pS=0; int pP=0;
	// -- variables to manipulate torroids colors dynamics
	int AtR; int AtG; int AtB; 	int MedR; int MedG; int MedB;
	int EEGindex=0;
	
	PFont f; 
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

	int ma_LastTime=0;  float ma_value = 0; int ma_length_ms=0;
	
	// -- mainGUI (3 icons moving on the triangle)
//	PVector[] coords1, coords2, coords3, allCoords;
//	float baseLength1, baseLength2, baseLength3, totalLength;
//	int count1, count2, count3, countAll;
//	float [] f1; 	float [] f2;	float [] f3; float [] fAllCoords;
		// -- speed of the moving icons in points
//	float RotationSpeed = 5;
	int trSideLength = 1000;
	
	// -- Sierpinski fractal iteration (from 0 to 7)
//	PVector vert1, vert2, vert3;
	int SierpF_iterN = 2; float DynamicIterrN=SierpF_iterN;
	/** array for vertices coordinates of main Sierpinski triangle */	  float [] SierpTrV = new float [6];
	/** icons width/hieght linked to main Sierpinski triangle radius */   int iconW, iconH; 
	int rMainF, rFixed, rSelectionF;
	
	//========================
	// -- processing algorithm
		// -- rotational acceleration settings, 0.00005f
	float rotationAccel = 0.0f, rotationAccelStep = (float) (0.001f*Math.PI/90f);	// ~ 0.000035 Radians
	float rotationAccelMin = 0f, rotationAccelMax =(float) (Math.PI/270f); 			// ~ 0.012 Radians
		// --if change in rotationAccel from 0 less then rotationAccelDeviation keep FirstRun=true
	float rotationAccelDeviation = 0.005f; 	
		// -- time to selecting command after rotationAccel become 0 
	float TimeToSelectMax = 70, TimeToSelect = TimeToSelectMax, TimeToSelectDecreaseStep = 0.5f;
		// -- rotational angle in degree 
	float rotationAngle = 0;  //float rotationAngle = 90; 
		
//	float rotationAngleDemo = 0;
	float demoTT = 5f; float demoTTd = 0.025f;
	
	boolean FirstRun = true; boolean action_cancel_flag = false;
	
	int mindOSlayer = 1;
	float AtDynamicAccDec = 0;
	/** -- index of selected items [0;2;4] */ 	int sItem = 0;
	/** -- index of L2 items to show [0;3;6] */   int Icon2DisplIndex=0;
	int temp=0;
	
	// -- Setting up a background and icons images
	PImage imgBack, imgCancel, imgBackforNum; 
	/** array for storing icons of the layer1*/ PImage[] imgL1 = new PImage[3]; 
	/** array for storing icons of the layer2*/ PImage[] imgL1L2 = new PImage[9]; 
	PImage imgMusic;
		PImage imgMusicPlay, imgMusicStop, imgMusicNext; 		
	PImage imgTerminal;
		PImage imgTerminalA, imgTerminalC, imgTerminalT; 
	PImage imgCamera;
//		PImage imgCameraPicture, imgCameraShare, imgCameraPrtSc; 
		PImage imgCameraPicture, imgCameraTwitter, imgCameraFB; 
	
	PImage imgApps;
		PImage imgAppsGoogle, imgAppsTerminal, imgAppsSettings; 
		
	// -- array for coordinates of the fist layer icons
	float [] iconFirstLayer = new float [6];  	
	// -- arrays for coordinates of the second layer icons
	float [] icon01SecondLayer = new float [6];
	float [] icon23SecondLayer = new float [6];
	float [] icon45SecondLayer = new float [6];

	float [] res = new float [6]; 

	
	public void setup(){	
		 // getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		// orientation(LANDSCAPE);
		 
//		  frameRate(15);  // Animate slowly
//		  noFill();
		  smooth();
		 // noStroke();
//		  colorMode(HSB, 8, 100, 100);
		  background(0,0,0); 
		  f = createFont("Georgia", 32,true); // STEP 3 Create Font
		  
		  rFixed = displayWidth/2 - 1*displayWidth/20;
		  rMainF = rFixed;	rSelectionF = 0;
		  
		  iconW = rFixed/5; iconH = rFixed/5;
		  
		  SierpTrV[0] = displayWidth/2 - 3*displayWidth/10;		SierpTrV[1] = displayHeight/2 + 1.5f*displayHeight/10;
		  SierpTrV[2] = displayWidth/2 + 3*displayWidth/10;		SierpTrV[3] = displayHeight/2 + 1.5f*displayHeight/10;
		  SierpTrV[4] = displayWidth/2;							SierpTrV[5] = displayHeight/2 - 1.5f*displayHeight/10;
		  		  
		  // -- using Processing's loadImage method to import an image and store it in the variable imgBack
		  imgBack = loadImage("blacksquare.png");
//		  imgBack = loadImage("blacksquare_big.png");
//		  imgBack = loadImage("b3_big.png");
		  imgCancel = loadImage("cancel.png");
		  imgBackforNum = loadImage("blacksquare2.png");
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
				  imgCameraTwitter = loadImage("twitter.png");
				  imgCameraFB = loadImage("facebook.png");
//		  imgTerminal = loadImage("google_search.png");
//				  imgTerminalA = loadImage("google.png");
//				  imgTerminalC = loadImage("gmail.png");
//				  imgTerminalT = loadImage("hangouts.png");
				  
		  imgApps = loadImage("icon_apps.png");
		  		  imgAppsGoogle = loadImage("google_search.png");
		  		  imgAppsTerminal = loadImage("icon_console.png"); 
		  		  imgAppsSettings = loadImage("settings1.png");
		  
		  		  // -- using arrays
		  imgL1[0] = imgMusic; imgL1[1] = imgCamera;  imgL1[2] = imgApps;
		  
		  imgL1L2[0] = imgMusicPlay; imgL1L2[1] = imgMusicStop;  imgL1L2[2] = imgMusicNext;
		  imgL1L2[3] = imgCameraPicture; imgL1L2[4] = imgCameraTwitter;  imgL1L2[5] = imgCameraFB;
		  imgL1L2[6] = imgAppsGoogle; imgL1L2[7] = imgAppsTerminal;  imgL1L2[8] = imgAppsSettings;
	}

	public void draw(){
		//getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		  // -- setup background color (when wo image)
		 // background(0,0,0); 
		  // -- basic lighting setup
		//  lights(); // -- not working on some devices
		  noFill();		  
		  // -- setup background image
		 // image(imgBack, 0, 0);
		   
		  //=========================================
		  	// -- get EEG index from background service and convert it to angular movement
		  EEGindex = getEEG();	 
		  //EEGindex = 65;
		  ProcessingAlgorithm();
		  rotationAngle = Algorithm.CircularMovement(rotationAngle, rotationAccel);
		  
			
		  //=========================================
//		  // -- DEMO mode only
//		  if (rotationAccel>0){ 
//			  if (rotationAngle>=360){rotationAngle=0;}
//			  rotationAngle = rotationAngle + rotationAccel;	
//		  }
//		  
//		  // -- for Demo
//		  rotationAngleDemo = rotationAngleDemo + rotationAccel;
//	  			// -- camera
////		  	  if (rotationAngleDemo>=50){rotationAccel = rotationAccel -0.001f;}
//			  	// -- music
////			  if (rotationAngleDemo>=150){rotationAccel = rotationAccel -0.001f;}
//		  		// -- apps
//			  if (rotationAngleDemo>=270){rotationAccel = rotationAccel -0.001f;}
//		 
////			  rotationAccel = 0;
		  //=========================================
			  
//		  res = IndexesOfSelectedIcons(rotationAngle, rotationAccel, SierpF_iterN, mindOSlayer, Icon2DisplIndex, sItem);
//		  rotationAccel = res[0]; SierpF_iterN = (int) res[1]; 
//		  mindOSlayer = (int) res[2]; Icon2DisplIndex = (int) res[3]; sItem = (int) res[4];
//			
		  //============================
		  	// -- main coordinates to draw main Sierpinski triangle
		  SierpTrV = getTriangleVertCoord(rotationAngle, displayWidth/2, displayHeight/2, rFixed, 0, 0);	  
		  		// -- icons of the first layer
		  iconFirstLayer = getTriangleVertCoord(rotationAngle, displayWidth/2, displayHeight/2, rFixed/2, iconW/2, iconH/2);
		  		// -- icons of the second layer // centerX+iW/2
		  icon01SecondLayer = getTriangleVertCoord(rotationAngle, iconFirstLayer[0]+iconW/2, iconFirstLayer[1]+iconH/2, rFixed/4, iconW/4, iconH/4);
		  icon23SecondLayer = getTriangleVertCoord(rotationAngle, iconFirstLayer[2]+iconW/2, iconFirstLayer[3]+iconH/2, rFixed/4, iconW/4, iconH/4);
		  icon45SecondLayer = getTriangleVertCoord(rotationAngle, iconFirstLayer[4]+iconW/2, iconFirstLayer[5]+iconH/2, rFixed/4, iconW/4, iconH/4);
		  
		  
		  //===================================================================
		  //===================================================================
		  // -- draw user interface (SierpTriangle, Icons, Animations)
		  if(mindOSlayer==1){
			  res = IndexesOfSelectedIcons(rotationAngle, rotationAccel, SierpF_iterN, mindOSlayer, Icon2DisplIndex, sItem);
			  rotationAccel = res[0]; SierpF_iterN = (int) res[1]; 
			  mindOSlayer = (int) res[2]; Icon2DisplIndex = (int) res[3]; sItem = (int) res[4];
				
			  
			  if(SierpF_iterN>=1 && SierpF_iterN<7){
				  rMainF=rFixed;
				  triangleSier(SierpTrV[0],SierpTrV[1],SierpTrV[2],SierpTrV[3],SierpTrV[4],SierpTrV[5],
						  SierpF_iterN, 0,0,0,false, 0,127,255,10);
				  
//				  drawFirstLayerIcons(imgMusic, imgCamera, imgApps);
				  drawFirstLayerIcons(imgL1[0], imgL1[1], imgL1[2]);
			  
				  	if(SierpF_iterN>=3){		
				  		drawSecondLayerIcons(imgL1L2[0], imgL1L2[1], imgL1L2[2],
				  							 imgL1L2[3], imgL1L2[4], imgL1L2[5],
				  						     imgL1L2[6], imgL1L2[7], imgL1L2[8]);		
				  		
				  	}
				  	if(SierpF_iterN>=4 && SierpF_iterN<=6 ){
					   // -- draw SierpFractal in different color to highlight selected item
					  	SierpTrV = getTriangleVertCoord(rotationAngle,iconFirstLayer[sItem]+iconW/2,iconFirstLayer[sItem+1]+iconH/2,rFixed/2,0,0);
					  	triangleSier(SierpTrV[0],SierpTrV[1],SierpTrV[2],SierpTrV[3],SierpTrV[4],SierpTrV[5],
					  				 SierpF_iterN-1, 0,0,0,true, 192,0,0,10);
					}
				  	
				  	if(SierpF_iterN>=2 && SierpF_iterN<5){ drawSectorLines();  	}
			  }
			  // -- transition to next level (animation)
			  if(SierpF_iterN==7){
				  	// -- decrease radius of active triangle (fade out)
					  rMainF = rMainF - 30;  if (rMainF<=0){rMainF = 0;}
					  SierpTrV = getTriangleVertCoord(rotationAngle, displayWidth/2, displayHeight/2, rMainF, 0, 0);
					  triangleSier(SierpTrV[0],SierpTrV[1],SierpTrV[2],SierpTrV[3],SierpTrV[4],SierpTrV[5],
							  SierpF_iterN, 47,47,79,false, 0,127,255,rMainF/100);
					  
					  rSelectionF = rSelectionF + 30;  if (rSelectionF>=rFixed){rSelectionF = rFixed;}
					  // -- zoom in fractal
					  SierpTrV = getTriangleVertCoord(rotationAngle, displayWidth/2, displayHeight/2, rSelectionF, 0, 0);
					  triangleSier(SierpTrV[0],SierpTrV[1],SierpTrV[2],SierpTrV[3],SierpTrV[4],SierpTrV[5],
							  2, 0,0,0,false, 0,127,255,10);
					  
					  if (rSelectionF==rFixed){
					      iconFirstLayer = getTriangleVertCoord(rotationAngle, displayWidth/2, displayHeight/2, rSelectionF/2, iconW/2, iconH/2);
					      // -- draw selected icons from second layer
					      drawFirstLayerIcons(imgL1L2[Icon2DisplIndex+0],imgL1L2[Icon2DisplIndex+1],imgL1L2[Icon2DisplIndex+2]);
					  }
			  }
			  
			  
//			  if(SierpF_iterN==7){	
//				  launchApp("com.aiworkereeg.launcher");
//				  
//			  }
			  
		  }
		  //===================================================================
		  //===================================================================
		  // -- next layer
		  if(mindOSlayer==2){
			  res = IndexesOfSelectedIcons(rotationAngle, rotationAccel, SierpF_iterN, mindOSlayer, Icon2DisplIndex, sItem);
			  rotationAccel = res[0]; SierpF_iterN = (int) res[1]; 
			  mindOSlayer = (int) res[2]; Icon2DisplIndex = (int) res[3]; sItem = (int) res[4];
			  
//			//  launchApp("com.facebook.katana");
			  if(SierpF_iterN>=1 && SierpF_iterN<7){
				  rMainF=rFixed;
				  SierpTrV = getTriangleVertCoord(rotationAngle, displayWidth/2, displayHeight/2, rFixed, 0, 0);
				  triangleSier(SierpTrV[0],SierpTrV[1],SierpTrV[2],SierpTrV[3],SierpTrV[4],SierpTrV[5],
						  SierpF_iterN, 0,0,0,false, 0,127,255,10);
				  
//				  drawFirstLayerIcons(imgMusicPlay, imgMusicStop, imgMusicNext);
				  drawFirstLayerIcons(imgL1L2[Icon2DisplIndex+0],imgL1L2[Icon2DisplIndex+1],imgL1L2[Icon2DisplIndex+2]);

//				  	if(SierpF_iterN>=3){		
////				  		drawSecondLayerIcons(imgL1L2[0], imgL1L2[1], imgL1L2[2],
////				  							 imgL1L2[3], imgL1L2[4], imgL1L2[5],
////				  						     imgL1L2[6], imgL1L2[7], imgL1L2[8]);	
//				  		drawFirstLayerIcons(imgL1L2[Icon2DisplIndex+0],imgL1L2[Icon2DisplIndex+1],imgL1L2[Icon2DisplIndex+2]);
//			  		}
				  
				  	if(SierpF_iterN>=4 && SierpF_iterN<=6 ){
					   // -- draw SierpFractal in different color to highlight selected item
					  	SierpTrV = getTriangleVertCoord(rotationAngle,iconFirstLayer[sItem]+iconW/2,iconFirstLayer[sItem+1]+iconH/2,rFixed/2,0,0);
					  	triangleSier(SierpTrV[0],SierpTrV[1],SierpTrV[2],SierpTrV[3],SierpTrV[4],SierpTrV[5],
					  				 SierpF_iterN-1, 0,0,0,true, 192,0,0,10);
					}
				  	
				  	if(SierpF_iterN>=2 && SierpF_iterN<5){ drawSectorLines();  	}
			  	
			  }			  		  
			  				

			  // -- transition to next level (animation)
			  if(SierpF_iterN==7){
				  	// -- decrease radius of active triangle (fade out)
					  rMainF = rMainF - 30;  if (rMainF<=0){rMainF = 0;}
					  SierpTrV = getTriangleVertCoord(rotationAngle, displayWidth/2, displayHeight/2, rMainF, 0, 0);
					  triangleSier(SierpTrV[0],SierpTrV[1],SierpTrV[2],SierpTrV[3],SierpTrV[4],SierpTrV[5],
							  SierpF_iterN, 47,47,79,false, 0,127,255,rMainF/100);
					  
					  rSelectionF = rSelectionF + 30;  if (rSelectionF>=rFixed){rSelectionF = rFixed;}
					  // -- zoom in fractal
					  SierpTrV = getTriangleVertCoord(rotationAngle, displayWidth/2, displayHeight/2, rSelectionF, 0, 0);
					  triangleSier(SierpTrV[0],SierpTrV[1],SierpTrV[2],SierpTrV[3],SierpTrV[4],SierpTrV[5],
							  1, 0,0,0,false, 0,127,255,10);
					  
//					  if (rSelectionF==rFixed){
////					      iconFirstLayer = getTriangleVertCoord(rotationAngle, displayWidth/2, displayHeight/2, rSelectionF/2, iconW/2, iconH/2);
//					      // -- draw selected icons from second layer
////					      drawFirstLayerIcons(imgL1L2[Icon2DisplIndex+0],imgL1L2[Icon2DisplIndex+1],imgL1L2[Icon2DisplIndex+2]);
//					      image(imgL1L2[Icon2DisplIndex+temp], displayWidth/2-iconW, displayHeight/2-iconH, 2*iconW, 2*iconH);
//					  }
			  }
			  
			  if(SierpF_iterN==8){	
//				  launchApp("com.aiworkereeg.launcher");
				  image(imgL1L2[Icon2DisplIndex+temp], displayWidth/2-iconW, displayHeight/2-iconH, 2*iconW, 2*iconH);
			  }

		  }
		  
		  // -- display cancel image (in case acceleration change from 0 before time to select become 0 )
//		  if(action_cancel_flag) {image(imgCancel, displayWidth/2-iconW, displayHeight/2-iconH, 2*iconW, 2*iconH);}
		  
		  // -- display EEG index
		  
		  switch(MainActivity.UserControl){
			case "att":
				if(EEGindex>=50){fill(0, 255, 0);//fill(0, 102, 153);
				} else{fill(255,255,255);}
				break;
			case "med":
				if(EEGindex>=50){fill(0, 255, 0);//fill(0, 102, 153);
				} else{fill(255,255,255);}	
				break;
			case "S":
				if(EEGindex>=-30 && EEGindex<=30){fill(0, 102, 153);} else{fill(255,255,255);}
				break;
		  }  
		  // -- for standard android devices
		  textFont(f,40);
		  image(imgBackforNum, displayWidth/2 + 0.8f*rFixed, displayHeight/2 - 0.9f*rFixed);
		  text(EEGindex, displayWidth/2 + 0.8f*rFixed, displayHeight/2 - 0.8f*rFixed);
		  
		  // -- for glass only
//		  textFont(f,25);
//		  image(imgBackforNum, displayWidth/2 + 0.73f*rFixed, displayHeight/2 - 1.6f*rFixed);
//		  text(EEGindex, displayWidth/2 + 0.73f*rFixed, displayHeight/2 - 0.8f*rFixed);
		  
		  
		  //============================
		  // -- developing messages
		 /* textFont(f,32);
		  fill(0, 102, 153);
		  text("t:"+ TimeToSelect,0, displayHeight/30); 
		  text(EEGindex ,0, displayHeight/20); 
		  text(rotationAccel ,0, displayHeight/15); 
		  text(SierpF_iterN ,0, displayHeight/12);
		  text(String.valueOf(FirstRun) ,0, displayHeight/10); */
		 // text(String.valueOf(rotationAngle) ,0, displayHeight/8); 
		  	  
		  	  
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
	/*public void calculatePathOnTriangle() {
		  // -- calculate length (in points) between vertices
//		  baseLength1 = PVector.dist(vert1, vert2);
//		  baseLength2 = PVector.dist(vert1, vert3) - 1;
//		  baseLength3 = PVector.dist(vert2, vert3) - 2;
//		  totalLength = baseLength1 + (baseLength2) + (baseLength3) ;
//
//		  Log.d(getString(R.string.app_name), "ir_d calculatePathOnTriangle()"+baseLength1);
//		  Log.d(getString(R.string.app_name), "ir_d calculatePathOnTriangle()"+baseLength2);
//		  Log.d(getString(R.string.app_name), "ir_d calculatePathOnTriangle()"+baseLength3);
//		  Log.d(getString(R.string.app_name), "ir_d calculatePathOnTriangle()"+totalLength);
		  
		  // fill base top coordinate array
		  totalLength = trSideLength + (trSideLength-1) + (trSideLength-2);
		  Log.d(getString(R.string.app_name), "ir_d calculatePathOnTriangle()"+totalLength);
		  int k = 0;
		  allCoords = new PVector[ceil(totalLength)];
		  
		  coords1 = new PVector[ceil(trSideLength)];
		  for (int i=0; i<coords1.length; i++) {
		    coords1[i] = new PVector();
		    coords1[i].x = vert1.x + i*(vert2.x-vert1.x)/trSideLength;
		    coords1[i].y = vert1.y + i*(vert2.y-vert1.y)/trSideLength; // -- 0 for horizontal side
		    // --
		    allCoords[k] = new PVector();
		    allCoords[k] = coords1[i];
		    k = k +1;
		    }
		  
		  coords2 = new PVector[ceil(trSideLength-1)];
		  for (int i=0; i<coords2.length; i++) {
		    coords2[i] = new PVector();
		    coords2[i].x = vert2.x - (i+1)*(vert2.x-vert3.x)/trSideLength;
		    coords2[i].y = vert2.y + (i+1)*(vert3.y-vert2.y)/trSideLength;
		    // --
		    allCoords[k] = new PVector();
		    allCoords[k] = coords2[i];
		    k = k +1;
		  }
		  		  
		  coords3 = new PVector[ceil(trSideLength-2)];
		  for (int i=0; i<coords3.length; i++) {
		    coords3[i] = new PVector();
		    coords3[i].x = vert3.x - (i+1)*(vert3.x-vert1.x)/trSideLength;
		    coords3[i].y = vert3.y - (i+1)*(vert3.y-vert1.y)/trSideLength;
		    
		    // --
		    allCoords[k] = new PVector();
		    allCoords[k] = coords3[i];
		    k = k + 1;
		  }
		 		  
//		  totalLength = k-1;
		}*/
	
	/** get EEG data from MainActivity and calculate S,P
	 *  moving average not working yet!!! */
	public int getEEG(){		
		if (millis() - ma_LastTime<=ma_length_ms){
		 	 switch(MainActivity.UserControl){
		 	 case "att":
		 		ma_value = 0.5f*(ma_value + eegService.At); 	
		 	 case "med":
		 		ma_value = 0.5f*(ma_value + eegService.Med); 
		 	 case "S":
		 		ma_value = 0.5f*(ma_value + (eegService.At - eegService.Med)); 
		 	 case "P":
		 		ma_value = 0.5f*(ma_value + (eegService.At + eegService.Med));
		 	 }
			 return 0;
		}else{
		
		 	 switch(MainActivity.UserControl){
		 	 case "att":
		 		ma_value = 0.5f*(ma_value + eegService.At); 
		 		return eegService.At;
		 	 case "med":
		 		ma_value = 0.5f*(ma_value + eegService.Med); 
		 		return eegService.Med;
		 	 case "S":
		 		ma_value = 0.5f*(ma_value + (eegService.At - eegService.Med)); 
		 		return (eegService.At - eegService.Med);
		 	 case "P":
		 		ma_value = 0.5f*(ma_value + (eegService.At + eegService.Med));
		 		return (eegService.At + eegService.Med);
		 	 default:
		 	     return 0;
		 	 }
		}
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
	
	/** draw icons of the first layer */
	public void drawFirstLayerIcons(PImage icon1, PImage icon2, PImage icon3){
		  image(icon1, iconFirstLayer[0], iconFirstLayer[1], iconW, iconH);
		  image(icon2, iconFirstLayer[2], iconFirstLayer[3], iconW, iconH);
		  image(icon3, iconFirstLayer[4], iconFirstLayer[5], iconW, iconH); 
	}

	/** draw icons of the second layer */
	public void drawSecondLayerIcons(PImage icon11, PImage icon12, PImage icon13,
									 PImage icon21, PImage icon22, PImage icon23,
									 PImage icon31, PImage icon32, PImage icon33){
		
		  image(icon11, icon01SecondLayer[0], icon01SecondLayer[1], iconW/2, iconH/2);
		  image(icon12, icon01SecondLayer[2], icon01SecondLayer[3], iconW/2, iconH/2); 
		  image(icon13, icon01SecondLayer[4], icon01SecondLayer[5], iconW/2, iconH/2);
		  
		  image(icon21, icon23SecondLayer[0], icon23SecondLayer[1], iconW/2, iconH/2);
		  image(icon22, icon23SecondLayer[2], icon23SecondLayer[3], iconW/2, iconH/2); 
		  image(icon23, icon23SecondLayer[4], icon23SecondLayer[5], iconW/2, iconH/2);
		  
		  image(icon31, icon45SecondLayer[0], icon45SecondLayer[1], iconW/2, iconH/2);
		  image(icon32, icon45SecondLayer[2], icon45SecondLayer[3], iconW/2, iconH/2); 
		  image(icon33, icon45SecondLayer[4], icon45SecondLayer[5], iconW/2, iconH/2); 
	}
	
	
	/** Processing Algorithm of the user activity */
	public void ProcessingAlgorithm(){
		// -- create dynamic 
	    switch(MainActivity.UserControl){
			case "att":
			  rotationAccel = Algorithm.AMtoDynamicMovement(EEGindex, rotationAccel,
					  rotationAccelMin, rotationAccelMax, rotationAccelStep, 50, 0);
			  break;
			case "med":
			  rotationAccel = Algorithm.AMtoDynamicMovement(EEGindex, rotationAccel,
					  rotationAccelMin, rotationAccelMax, rotationAccelStep, 50, 0);	
			  break;
			case "S":
			  rotationAccel = Algorithm.StoDynamicMovement(EEGindex, rotationAccel,
					  rotationAccelMin, rotationAccelMax, rotationAccelStep, -30, 30, 0);
			  break;
		}
	  	    
		// -- setup number of Fractal iterations to show depending on rotational acceleration
//		if (rotationAccel<=0){rotationAccel = 0;}
//		if (rotationAccel>=rotationAccelDeviation){SierpF_iterN = 2;}
//		if (rotationAccel>0.0f && rotationAccel<rotationAccelDeviation && FirstRun==true){SierpF_iterN = 2;}
//		if (rotationAccel>0.0f && rotationAccel<rotationAccelDeviation && FirstRun==false){SierpF_iterN = 3;}
		
		
		// -- check if user want to send command
    		// -- check if rotational speed = 0 
	    if (rotationAccel<=0f && FirstRun==false){
	    	action_cancel_flag = false;
	    		// -- decrease time to selecting command
//	    	TimeToSelect = TimeToSelect - TimeToSelectDecreaseStep;
//	    	if (TimeToSelect<0f){ TimeToSelect = 0f;}
	    		// -- display time to select by changing Fractal iteration
	    	//SierpF_iterN = (int) (7 - TimeToSelect/10);
			TimeToSelect = TimeToSelect - TimeToSelectDecreaseStep;
			if (TimeToSelect>=55f && TimeToSelect<70f){SierpF_iterN = 4;}
			if (TimeToSelect>=40f && TimeToSelect<55f){SierpF_iterN = 5;}
			if (TimeToSelect>=25f && TimeToSelect<40f){SierpF_iterN = 6;}
			if (TimeToSelect>=10f && TimeToSelect<25f){SierpF_iterN = 7;}
			if (TimeToSelect<=0){TimeToSelect = 0;}
	    }
	    
	    
	    	// -- cancel command selection if rotational speed are not 0 anymore
	    if (rotationAccel>0f && TimeToSelect>0f && TimeToSelect<TimeToSelectMax){
	    		 // -- reset parameters
	    		 action_cancel_flag = true; 
	    		 TimeToSelect = TimeToSelectMax;
	    		 SierpF_iterN = 2;
	    		 FirstRun = true;	   
	    		 
	    }
	    
	    	// -- prevent selecting command till rotationAccel will sufficiently increase
	    if (TimeToSelect == TimeToSelectMax && rotationAccel>rotationAccelDeviation){
	    	FirstRun = false;
	    }
	    
	    if (rotationAccel>rotationAccelDeviation){
   		 		 SierpF_iterN = 0; 
   		 		 TimeToSelect = TimeToSelectMax;
   		}
	   
	}
			
	
	/** get indexes of selected items to properly display them */
	public float[] IndexesOfSelectedIcons(float rAngle, float rAccel, int SierpFiterN, int mindOSlayerl,
										  int Icon2DisplIndexl, int sIteml){
		float[] resl = new float[6]; 
		
		// -- setup number of Fractal iterations to show depending on rotational acceleration
		if (rAccel<=0){rAccel = 0;}
		if (rAccel>=rotationAccelDeviation){SierpFiterN = 2;  action_cancel_flag = false;}
		if (rAccel>0.0f && rAccel<rotationAccelDeviation && FirstRun==true){SierpFiterN = 2;} 
		if (rAccel>0.0f && rAccel<rotationAccelDeviation && FirstRun==false){SierpFiterN = 3;}
		
			  // -- process of selecting items when rotation stops
//		if (rAccel==0 && FirstRun == false && TimeToSelect==0){		  
//			demoTT = demoTT - demoTTd;
//			if (demoTT<=0){demoTT = 0;}
//			if (demoTT>=4f && demoTT<4.5f){SierpFiterN = 4;}
//			if (demoTT>=3f && demoTT<4f){SierpFiterN = 5;}
//			if (demoTT>=2f && demoTT<3f){SierpFiterN = 6;}
//			if (demoTT>=1f && demoTT<2f){SierpFiterN = 7;}
			
		if (rAccel==0 && FirstRun == false){	
//			TimeToSelect = TimeToSelect - TimeToSelectDecreaseStep;
//			if (TimeToSelect>=55f && TimeToSelect<70f){SierpFiterN = 4;}
//			if (TimeToSelect>=40f && TimeToSelect<55f){SierpFiterN = 5;}
//			if (TimeToSelect>=25f && TimeToSelect<40f){SierpFiterN = 6;}
//			if (TimeToSelect>=10f && TimeToSelect<25f){SierpFiterN = 7;}
//			if (TimeToSelect<=0){TimeToSelect = 0;}
			
//			if (demoTT>=0f && demoTT<1f){SierpFiterN = 8; mindOSlayerl=2; rAccel = 0.5f; demoTT=5f;}
		  

			if(mindOSlayerl==1){
//				if (demoTT>=0f && demoTT<1f){
				if (TimeToSelect == 0){
					SierpFiterN = 8; mindOSlayerl=2;
//					rAccel = 0.5f; 
//					demoTT=5f;
					TimeToSelect=rotationAccelMax;
					action_cancel_flag = false;
					FirstRun = true;
				}
				
				if (rAngle>300 || rAngle<=60){Icon2DisplIndexl = 0;  sIteml = 0;}
				if (rAngle>60 && rAngle<=180){Icon2DisplIndexl = 6;  sIteml = 4;}
				if (rAngle>180 && rAngle<=300){Icon2DisplIndexl = 3; sIteml = 2;}
			}
			
			
			if(mindOSlayerl==2 && FirstRun==false){
//				if (demoTT>=0f && demoTT<1f){
				if (TimeToSelect == 0){
					SierpFiterN = 8; mindOSlayerl=3;
//					rAccel = 0.5f;
					TimeToSelect=rotationAccelMax;
					action_cancel_flag = false;
//					demoTT=5f;
				}
				
				if (rAngle>300 || rAngle<=60){sIteml = 0;  temp = 0; }
				if (rAngle>180 && rAngle<=300){sIteml = 2; temp = 1;}
				if (rAngle>60 && rAngle<=180){sIteml = 4; temp = 2;}
				
			}
			
		}
		
		// -- create array for return statement
		resl[0]= rAccel; resl[1]= SierpFiterN; resl[2]= mindOSlayerl; 
		resl[3] = Icon2DisplIndexl; resl[4] = sIteml;
		return resl;
	   
	}
	
	
	/** draw sector lines for selected items */
	public void drawSectorLines(){
	  	// -- draw sector lines
		stroke(176,48,96);    strokeWeight(10);  strokeCap(ROUND);
	  	line((float) (displayWidth/2 + (rFixed-displayWidth/20) * Math.sin(Math.toRadians(60)) ), 
	  		 (float) (displayHeight/2 + (rFixed-displayWidth/20) * Math.cos(Math.toRadians(60)) ), 
	  		 (float) (displayWidth/2 + rFixed * Math.sin(Math.toRadians(60)) ), 
	  		 (float) (displayHeight/2 + rFixed * Math.cos(Math.toRadians(60)) ));

	  	stroke(255,255,255);
	  	line((float) (displayWidth/2 + (rFixed-displayWidth/20) * Math.sin(Math.toRadians(180)) ), 
		  		 (float) (displayHeight/2 + (rFixed-displayWidth/20) * Math.cos(Math.toRadians(180)) ), 
		  		 (float) (displayWidth/2 + rFixed * Math.sin(Math.toRadians(180)) ), 
		  		 (float) (displayHeight/2 + rFixed * Math.cos(Math.toRadians(180)) ));
	  	
	  	stroke(176,48,96);
	  	line((float) (displayWidth/2 + (rFixed-displayWidth/20) * Math.sin(Math.toRadians(300)) ), 
	  		 (float) (displayHeight/2 + (rFixed-displayWidth/20) * Math.cos(Math.toRadians(300)) ), 
	  		 (float) (displayWidth/2 + rFixed * Math.sin(Math.toRadians(300)) ), 
	  		 (float) (displayHeight/2 + rFixed * Math.cos(Math.toRadians(300)) ));
	}
	
	protected void launchApp(String packageName) {
		        Intent mIntent = getPackageManager().getLaunchIntentForPackage(packageName);
		        if (mIntent != null) {
		        	try {
		                startActivity(mIntent);
		            } catch (ActivityNotFoundException err) {
		                Toast t = Toast.makeText(getApplicationContext(),
		                        "app not found", Toast.LENGTH_SHORT);
		                t.show();
		            }
		        }
	}


	/*public void AttAccDeceleration(){
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
	     
	}*/
	
	
	
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
