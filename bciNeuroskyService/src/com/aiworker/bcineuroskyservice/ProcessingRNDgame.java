package com.aiworker.bcineuroskyservice;

import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.util.Random;
import java.util.concurrent.TimeUnit;
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
		
		Random r = new Random(); private long mLastTime; int rndUpdDelay_ms = 1000;
		private long DataCollectionLastTime; int DataCollectionDelay_ms = 1000;
		private long CurrentTime,TimeOfTheGame = 0;
		int GameLevel = 0; int MaxGameLevel=9; String rndMean = "mean";
		int ma_LastTime=0;  float ma_value = 0; int ma_length_ms=0;
		
		// -- toroids setting
		int pts = 10; float angle = 0; float latheAngle = 0;
			// -- internal and external radius
//		float radiusAt = 10.0f; float radiusMed = 10.0f;
//		float latheRadiusAt = 50.0f;
//		int segments = 40;
//		float ExternalToroidRadius = 50.0f;
		int segments = 20;
		float ExternalToroidRadius = 30.0f;
			// -- dynamic internal radius
//		float AtDynamicR = radiusAt; float MedDynamicR = radiusMed;
			// -- for optional helix
		boolean isHelix = false;	float helixOffset = 5.0f;
			// -- toroids vertices
		PVector vertices[], vertices2[];
			// -- network game play
		float Player2Accel = 0; float localPlayerAccel = 0;
		
			// -- EEGindex graph
		PFont f; 
		int legend100,legend60, legend40, legend0, legendAdjY = 5, legendAdjX = 70;
		int waveHigh; // vertical distance between line 0 and 100 <-> historical wave amplitude
		// -- for wave
//		int xspacing = 8;   // How far apart should each horizontal location be spaced
//		int xspacing = 20; 
		int w;              // Width of entire wave
//		int waveLength=200;
//		int maxwaves = 4;   // total # of waves to add together

//		float theta = 0.0f;
//		float[] amplitude = new float[maxwaves];   // Height of wave
//		float[] dx = new float[maxwaves];          // Value for incrementing X, to be calculated as a function of period and xspacing
		float[] yvalues;                           // Using an array to store height values for the wave (not entirely necessary)
		float[] xvalues;
		
		PImage imgFinish; 
		
		
		public void setup(){	 
//			 frameRate(15); 
			 smooth(); // noStroke();
			 // colorMode(HSB, 8, 100, 100);
			 colorMode(RGB, 255, 255, 255, 100);
			 
			 // -- setup historical wave graph size
			 waveHigh = displayHeight/10; 
			 
			 
			 f = createFont("Arial",16,true); // STEP 3 Create Font
			 DataCollectionLastTime = millis();
			 CurrentTime = millis();
			 ma_LastTime = millis();
			 // -- for wave
				 w = 40 ;
				 xvalues = new float[w];
				 xvalues[0] = 0;
				 xvalues[1] = legendAdjX;
				 
				 for (int i = 2; i <w; i++) { 
				   xvalues[i] = xvalues[i-1] + displayWidth/(w);
				 }
				 yvalues = new float[w];
			 // -- end for wave
			 
				 imgFinish = loadImage("finish.png");
			 

				 legend100 = 50;
				 legend60 = (int) (legend100 + 0.4f*waveHigh);
				 legend40 = (int) (legend100 + 0.6f*waveHigh);
				 legend0 = (int) (legend100 + 1f*waveHigh);
				 
		}

		public void draw(){
			  // -- draw background and setup basic lighting setup
			  background(0);
			  //lights();  // -- not working on some devices
			  
//			  displayHeight - 1*displayHeight/10 - Player2Accel
			  // -- check game status
			  if (localPlayerAccel>Player2Accel && (displayHeight - 1*displayHeight/10 - localPlayerAccel)<=(2f*displayHeight/10))
			  	{Player2Accel = 0; localPlayerAccel = 0; GameLevel = GameLevel + 1; CurrentTime=millis();} 
			  if (Player2Accel>localPlayerAccel && (displayHeight - 1*displayHeight/10 - Player2Accel)<=(2f*displayHeight/10))
			  	{Player2Accel = 0; localPlayerAccel = 0; GameLevel = GameLevel - 1; CurrentTime=millis();} 
			  if (GameLevel<0)	{GameLevel = 0;} 
			  
			  // ===============================================
			  		// -- display finish line
			  image(imgFinish, 0, 2.0f*displayHeight/10, displayWidth, 0.5f*displayHeight/10);
		  
			  // ===============================================
			  		// -- display game time
			  TimeOfTheGame = millis() - CurrentTime;
			  textFont(f,32);                 // STEP 4 Specify font to be used
			  fill(255);                        // STEP 5 Specify font color 
	
			  String s = String.format("%02d:%02d:%02d", 
					  TimeUnit.MILLISECONDS.toHours(TimeOfTheGame),
					  TimeUnit.MILLISECONDS.toMinutes(TimeOfTheGame) -  
					  TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(TimeOfTheGame)), // The change is in this line
					  TimeUnit.MILLISECONDS.toSeconds(TimeOfTheGame) - 
					  TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(TimeOfTheGame))); 
			  
			  text(s,4.5f*displayWidth/10, 2.0f*displayHeight/10 - 20f);  
			  // ===============================================
			  		// -- get EEG index (one from att/med/S/P)
			  index = getEEG();
			  indexPl2 = getEEGNetworkUser();		  
			  
			  switch(MainActivity.toroidGameType){
				    case "you":
				    	displayLocalUserToroid(displayWidth/2,displayHeight - 1*displayHeight/10 - localPlayerAccel);
				    	break;
				    	
					case "rnd vs you":
							// -- local
						displayLocalUserToroid(displayWidth/2 + 2f*displayWidth/10,displayHeight - 1*displayHeight/10 - localPlayerAccel);
							// -- RND
//						displayPlayer2Toroid(displayWidth/2 - 3f*displayWidth/10, displayHeight - 1*displayHeight/10 - Player2Accel);
							// --networkUser
						displayNetworklUserToroid(displayWidth/2 - 3f*displayWidth/10, displayHeight - 1*displayHeight/10 - Player2Accel);
						
						displayGameLevel();
						break;
			  } 
			 
			// ===============================================
//			  DataCollection(indexPl2);	
			  DataCollection(index);	
			  displayGraphOfEEGindex();


		}

		public void thoroid (int _positionX, int _positionY, int _R, int _G, int _B, boolean isWireFrame_l,
				float radius_l, float latheRadius_l) {
		  // -- 2 rendering styles: wireframe or solid
			strokeWeight(1);
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
		  noFill();
		}

		/** draw wave using ellipse */
		void displayGraphOfEEGindex() {
			  // -- draw EEGindex as a curve
			  noSmooth();  
			  noFill();
			  strokeWeight(5); 
			  stroke(200,255,200);
//			  curveTightness(1.25f);
			  beginShape();
			  for (int i = 0; i < yvalues.length; i++) {				  
//			    	if(yvalues[i]>80 && yvalues[i]<120){stroke(255,0,0);}
//				  if(yvalues[i]<40){stroke(0,0,255);}
//				  if(yvalues[i]>=40 && yvalues[i]<=60){stroke(170,170,170);}
//				  if(yvalues[i]>60){stroke(0,255,0);}
//				  indexR = (255 * index) / 100; indexG = 0;  indexB = (255 * (100 - index)) / 100 ;
//				  indexR = (255 * indexPl2 ) / 100; indexG = 0;  indexB = (255 * (100 - indexPl2 )) / 100 ;
//				  stroke(indexR, indexG, indexB);
				  
//				curveVertex(xvalues[i], 1.5f*displayHeight/10 - yvalues[i]); // the first control point
//				  if(i<5){strokeWeight(0);} else {strokeWeight(5);}
				curveVertex(xvalues[i], legend0 - yvalues[i]);

			  }
			  endShape();
			 
			  // -- draw chart legends
			  switch(MainActivity.UserControl){
			 	 case "att":
					  textFont(f,32);  fill(255);	text(" 0",10f, legend0+legendAdjY);  
					  textFont(f,20);  fill(190);	text("  40",10f, legend40+legendAdjY); 
					  textFont(f,20);  fill(190);	text("  60",10f, legend60+legendAdjY); 
					  textFont(f,32);  fill(255);	text("100",10f, legend100+legendAdjY); 
					  break;
			 	 case "med":
					  textFont(f,32);  fill(255);	text(" 0",10f, legend0+legendAdjY);  
					  textFont(f,20);  fill(190);	text("  40",10f, legend40+legendAdjY); 
					  textFont(f,20);  fill(190);	text("  60",10f, legend60+legendAdjY); 
					  textFont(f,32);  fill(255);	text("100",10f, legend100+legendAdjY); 
					  break;
			 	 case "S":
					  textFont(f,32);  fill(255);	text("-100",10f, legend0+legendAdjY);  
					  textFont(f,20);  fill(190);	text(" -30",10f, legend40+legendAdjY); 
					  textFont(f,20);  fill(190);	text(" +30",10f, legend60+legendAdjY); 
					  textFont(f,32);  fill(255);	text(" 100",10f, legend100+legendAdjY); 
					  break;
			 	 case "P":
					  textFont(f,32);  fill(255);	text("  0",10f, legend0+legendAdjY);  
					  textFont(f,20);  fill(190);	text(" 80",10f, legend40+legendAdjY); 
					  textFont(f,20);  fill(190);	text("120",10f, legend60+legendAdjY); 
					  textFont(f,32);  fill(255);	text("200",10f, legend100+legendAdjY); 
					  break;
			  }
			  // -- draw chart lines
			  stroke(170,170,170); strokeWeight(2f); line(legendAdjX, legend0, displayWidth-legendAdjX*0.1f, legend0);
			  stroke(170,170,170); strokeWeight(2f); line(legendAdjX, legend100, displayWidth-legendAdjX*0.1f, legend100);
			  stroke(170,170,170); strokeWeight(1f); line(legendAdjX, legend60, displayWidth-legendAdjX*0.1f, legend60);
			  stroke(170,170,170); strokeWeight(1f); line(legendAdjX, legend40, displayWidth-legendAdjX*0.1f, legend40);
			  		  
			  // -- draw rectangular box on chart
			  fill(170,170,170, 40); noStroke();
//			  rect(100f, legend60, displayWidth, legend40);
			  rect(legendAdjX, legend40, displayWidth-legendAdjX*1.1f, legend60-legend40);
			  
			  
		}
		
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
		
		/** get EEG data from MainActivity and calculate S,P
		 *  moving average not working yet!!! */
		public int getEEGNetworkUser(){		
			if (millis() - ma_LastTime<=ma_length_ms){
			 	 switch(MainActivity.UserControl){
			 	 case "att":
			 		ma_value = 0.5f*(ma_value + eegService.At_pl2); 	
			 	 case "med":
			 		ma_value = 0.5f*(ma_value + eegService.Med_pl2); 
			 	 case "S":
			 		ma_value = 0.5f*(ma_value + (eegService.At_pl2 - eegService.Med_pl2)); 
			 	 case "P":
			 		ma_value = 0.5f*(ma_value + (eegService.At_pl2 + eegService.Med_pl2));
			 	 }
				 return 0;
			}else{
			
			 	 switch(MainActivity.UserControl){
			 	 case "att":
			 		ma_value = 0.5f*(ma_value + eegService.At_pl2); 
			 		return eegService.At_pl2;
			 	 case "med":
			 		ma_value = 0.5f*(ma_value + eegService.Med_pl2); 
			 		return eegService.Med_pl2;
			 	 case "S":
			 		ma_value = 0.5f*(ma_value + (eegService.At_pl2 - eegService.Med_pl2)); 
			 		return (eegService.At_pl2 - eegService.Med_pl2);
			 	 case "P":
			 		ma_value = 0.5f*(ma_value + (eegService.At_pl2 + eegService.Med_pl2));
			 		return (eegService.At_pl2 + eegService.Med_pl2);
			 	 default:
			 	     return 0;
			 	 }
			}
		}		
		
		
		
		public void DataCollection(int ind){
	  		// -- collect indexes
			  if (millis() - DataCollectionLastTime < DataCollectionDelay_ms) return; 
			  else{		
				  switch(MainActivity.UserControl){
				 	 case "att": yvalues[yvalues.length-1] = (waveHigh/100)*ind; break;
				 	 case "med": yvalues[yvalues.length-1] = (waveHigh/100)*ind; break;
				 	 case "S": yvalues[yvalues.length-1] = (waveHigh/100)*(ind+100)/2; break;
				 	 case "P": yvalues[yvalues.length-1] = (waveHigh/100)*ind/2; break;
				  }
//				  yvalues[yvalues.length-1] = (waveHigh/100)*ind;
//				  yvalues[yvalues.length-1] = 200;
				  
				  for (int j = 0; j < yvalues.length-1; j++) {
					  yvalues[j] = yvalues[j+1];				    	
				  }
//				  yvalues[yvalues.length-1] = ind*5;
				  
				  DataCollectionLastTime=millis();	
//				  
			  }
		}
			
		/** get random (Normal) distribution for userControl */
		public void getRndNormalDistribution(){			
			// -- create Randomly distributed time series
			  if (millis() - mLastTime < rndUpdDelay_ms) return; 
			  else  mLastTime = millis();
			  
			  
			double val = 0;
			switch(MainActivity.UserControl){
			 case "att":
				 val = r.nextGaussian() * 25 + (50+GameLevel*5); // 50 (mean); 25 (standard deviation) - 70% of data
				 indexPl2  = (int) Math.round(val);
				 if (indexPl2 >100) {indexPl2 =100;} if (indexPl2 <0) {indexPl2 =0;}	           
//				 if (mLastTime>rndUpdDelay_ms){mLastTime=0;}
				 rndMean = "mean ~ " + String.valueOf(50+GameLevel*5);
			  	 break;
			 case "med":
				 val = r.nextGaussian() * 25 + (50+GameLevel*5); // 50 (mean); 25 (standard deviation) - 70% of data
				 indexPl2  = (int) Math.round(val);
				 if (indexPl2 >100) {indexPl2 =100;} if (indexPl2 <0) {indexPl2 =0;}	           
//				 if (mLastTime>rndUpdDelay_ms){mLastTime=0;}
				 rndMean = "mean ~ " + String.valueOf(50+GameLevel*5);
				 break;
			 case "S":
				 val = r.nextGaussian() * 25 + (50+GameLevel*5); // 50 (mean); 25 (standard deviation) - 70% of data
				 indexPl2  = (int) Math.round(val);
				 if (indexPl2 >100) {indexPl2 =100;} if (indexPl2 <0) {indexPl2 =0;}	
				 indexPl2 = indexPl2*2 - 100; // adjust to interval -100:100 
//				 if (mLastTime>rndUpdDelay_ms){mLastTime=0;}
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
				  thoroid(0,0, indexR, indexG, indexB, true, ExternalToroidRadius-10, ExternalToroidRadius);
			  popMatrix();
			  			
//			  textFont(f,32);                 // STEP 4 Specify font to be used
			  textFont(f,displayHeight/50); 
			  fill(255);                        // STEP 5 Specify font color 
			  text(MainActivity.UserControl + " -> " + index+"\n    (you)",displayWidth/2 + 2.5f*displayWidth/10,displayHeight - 1.5f*displayHeight/10 - localPlayerAccel);  // STEP 6 Display Text
		}
		
		/** display toroid of the Network player */
		public void displayNetworklUserToroid(float x, float y){
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
				  translate(x, y);
				  //			  rotateZ(0);		  rotateY(0);		  rotateX(0);
				  rotateZ(frameCount*PI/170); rotateY(frameCount*PI/170); rotateX(frameCount*PI/170);
				  thoroid(0,0, indexR, indexG, indexB, true, ExternalToroidRadius-10, ExternalToroidRadius);
			  popMatrix();
			
//			  textFont(f,32);  
			  textFont(f,displayHeight/50); 
			  fill(255);                   
			  text(MainActivity.UserControl + " -> " + indexPl2+"\n    ("+rndMean+")",displayWidth/2 - 2.5f*displayWidth/10,displayHeight - 1.5f*displayHeight/10 - Player2Accel);
			  
		}
		
		/** display toroid of the second/rnd player */
		public void displayPlayer2Toroid(float x, float y){
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
				  translate(x, y);
				  //			  rotateZ(0);		  rotateY(0);		  rotateX(0);
				  rotateZ(frameCount*PI/170); rotateY(frameCount*PI/170); rotateX(frameCount*PI/170);
				  thoroid(0,0, indexR, indexG, indexB, true, ExternalToroidRadius-10, ExternalToroidRadius);
			  popMatrix();
			
//			  textFont(f,32);  
			  textFont(f,displayHeight/50); 
			  fill(255);                   
			  text(MainActivity.UserControl + " -> " + indexPl2+"\n    ("+rndMean+")",displayWidth/2 - 2.5f*displayWidth/10,displayHeight - 1.5f*displayHeight/10 - Player2Accel);
			  
		}
		
		/** display game level for rnd vs you game */
		public void displayGameLevel(){
			  noStroke();
			  for(int i=0; i<MaxGameLevel; i++){
					fill(255,0,0); 
					ellipse(1f*displayWidth/10 + i*displayWidth/10, displayHeight - 0.2f*displayHeight/10, 15, 15);  // Draw white ellipse using RADIUS mode
		  	  }
			  for(int i=0; i<GameLevel; i++){
					fill(0,0,255);  // Set fill to gray
					ellipse(1f*displayWidth/10 + i*displayWidth/10, displayHeight - 0.2f*displayHeight/10, 20, 20);  // Draw gray ellipse using CENTER mode
			  }
		}

		
		
		public int sketchWidth() { return displayWidth; }
		public int sketchHeight() { return displayHeight; }
		public String sketchRenderer() { return P3D; }	
		
}