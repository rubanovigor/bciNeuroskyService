package com.aiworker.bcineuroskyservice;


import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.Arrays;
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

import android.content.Intent;
import android.view.KeyEvent;


/** activity for Network Game Sample        */
public class ProcessingVisualizer extends PApplet{
		// -- local EEG variables
		int pAt=0; int pMed=0; int pS=0; int pP=0;
		int indexRND=0, indexNetworkPlayer=0;
		int indexLocalPlayer[] = new int[] {0,0,0,0};
		String Torroid1info = "", Torroid2info = "";
		String playerInfor[] = new String[] {"you","pl1","pl2","pl3"};
		float plPositionY[] = new float[] {0,0,0,0};
		int plScore[] = new int[] {0,0,0,0};
		int playerID;
		float torroidX = 0, torroidY = 0;
		float sphereR;
		// -- position of vertical (Att) line
		float X_line, Y_line, line_width, line_hight;
		
			// -- variables to manipulate torroids colors dynamics
		int indexR, indexG, indexB; 
		int MedR; int MedG; int MedB;
		
		Random r = new Random(); private long mLastTime; int rndUpdDelay_ms = 1000;
		private long DataCollectionLastTime, DataCollectionLastTimeLocalPlayer,DataCollection;
		int DataCollectionDelay_ms = 1000, OneMin = 60000;
		private long CurrentTime,TimeOfTheGame = 0;
		int GameLevel = 1; int MaxGameLevel=4; 
		int ma_LastTime=0;  float ma_value = 0; int ma_length_ms=0;
		
		// -- toroids setting
		int pts = 10; float angle = 0; float latheAngle = 0;
			// -- internal and external radius
		int segments = 20;
		float ExternalToroidRadius, InternalToroidDelta;
			// -- dynamic internal radius
//		float AtDynamicR = radiusAt; float MedDynamicR = radiusMed;
			// -- for optional helix
		boolean isHelix = false;	float helixOffset = 5.0f;
			// -- toroids vertices
		PVector vertices[], vertices2[];
			// -- network game play
		float Player2Accel = 0; 	float localPlayerAccel = 0;

		
			// -- EEGindex graph
		PFont f; 
		float histChartR, histChartRsmall;							// -- big and small radius of the historical chart
		float finishLineY1coordinate, finishLineY2coordinate;	
		int EEGfraphHistLength = 85;         							// -- length of array for collecting historical indexes 
		int arrD = 5;
	
		float[] LocalPlayerYvalues; // RNDYvalues;  			// -- array for collecting hist data
		float[][] LocalPlayerColorsValues; // RNDColorsValues; // -- array for collecting colors for hist data
		
		float alpha = 0f;
		
		PImage imgFinish; 
		
			// -- array for data collection and moving average 
		float[] histData1min, histData10min, histData20min, histData30min;
		float[][] movingAvgHistData;
		
		String music_play_flag = "stop";
		
		public void setup(){	 
//			 frameRate(15); 
			 smooth(); // noStroke();
			 // colorMode(HSB, 8, 100, 100);
			 colorMode(RGB, 255, 255, 255, 100);
			 
			 // -- setup historical wave graph size
//			 histChartR = (int)displayHeight/10;
			 histChartR = (displayHeight*1f)/13f;
			 histChartRsmall = histChartR/4;
			 finishLineY1coordinate = 2*histChartR + 3*histChartRsmall;
			 finishLineY2coordinate = 2*histChartRsmall;
			 
			 ExternalToroidRadius = (displayWidth*1f)/25f;
			 InternalToroidDelta = histChartRsmall/2;
			 
			 f = createFont("Arial Bold",40,true); // STEP 3 Create Font
			 DataCollectionLastTime = millis();
			 DataCollection = millis();
			 DataCollectionLastTimeLocalPlayer = millis();
			 CurrentTime = millis();
			 ma_LastTime = millis();
			 // -- for wave
//				 EEGfraphHistLength = 90 ;
//				 xvalues = new float[EEGfraphHistLength];
//				 xvalues[0] = 0;
//				 xvalues[1] = legendAdjX;
//				 
//				 for (int i = 2; i <EEGfraphHistLength; i++) { 
//				   xvalues[i] = xvalues[i-1] + displayWidth/(EEGfraphHistLength);
//				 }
				 
			 LocalPlayerYvalues = new float[EEGfraphHistLength];
			 LocalPlayerColorsValues = new float[EEGfraphHistLength][3];
			
			 histData1min = new float[60]; Arrays.fill(histData1min, 0);
			 histData10min = new float[600]; Arrays.fill(histData10min, 0);
			 histData20min = new float[1200];
			 histData30min = new float[1800];
			 
			 movingAvgHistData = new float[arrD][EEGfraphHistLength]; //Arrays.fill(movingAvgHistData, 100);
			 
//			 RNDYvalues = new float[EEGfraphHistLength];
//			 RNDColorsValues = new float[EEGfraphHistLength][3];
				 
			 // -- finish line			 
//			 imgFinish = loadImage("finish.png");
			 
			 // -- verc
			 sphereR = displayWidth/20;
			 torroidX = displayWidth/4;
			 X_line = torroidX - displayWidth/80;
			 Y_line = displayHeight/8;
			 line_width = displayWidth/60;
			 line_hight = displayHeight - 2*displayHeight/8;		 
		 	
			
		}

		public void draw(){
			  // -- draw background and setup basic lighting setup
			  background(0);
			  
			  //lights();  // -- not working on some devices
		  
			  
//			  // -- get index of single race winner
//			  float largest = plPositionY[0]; int WinnerIndex = 0;
//			  for (int i = 1; i < plPositionY.length; i++) {
//			    if ( plPositionY[i] >= largest ) {
//			        largest = plPositionY[i];
//			        WinnerIndex = i;
//			     }
//			  }
//			  		// -- update parameters when single race is finished
//			  if ((displayHeight - 1*displayHeight/10 - ExternalToroidRadius-InternalToroidDelta - plPositionY[WinnerIndex])<=
//					  (finishLineY1coordinate+finishLineY2coordinate)){				  
//				  plScore[WinnerIndex] =  plScore[WinnerIndex] + 1;
//				  Arrays.fill(plPositionY, 0f);
//				  GameLevel = GameLevel + 1; 
//				  CurrentTime=millis();
//			  }
			  
			  		  
//			  if (GameLevel>MaxGameLevel)	{GameLevel = MaxGameLevel;} 
			  
			  // -- get index of single race winner
//			  float l = plScore[0]; int WI = 0;
//			  for (int i = 1; i < plScore.length; i++) {
//			    if ( plScore[i] >= l ) {
//			        l = plScore[i];
//			        WI = i;
//			     }
//			  }
			  
			  // ===============================================
			  		// -- display finish line
//			  image(imgFinish, 0, 2.0f*displayHeight/10, displayWidth, 0.5f*displayHeight/10);
//			  image(imgFinish, 0, finishLineY1coordinate, displayWidth, finishLineY2coordinate);
		  
//			  4/20 vs 1/20
			  // ===============================================
			  		// -- display game time
			  TimeOfTheGame = millis() - CurrentTime;
			  textFont(f,displayHeight/30);                 // STEP 4 Specify font to be used
			  fill(255);                        // STEP 5 Specify font color 
	
			  String s = String.format("%02d:%02d:%02d", 
					  TimeUnit.MILLISECONDS.toHours(TimeOfTheGame),
					  TimeUnit.MILLISECONDS.toMinutes(TimeOfTheGame) -  
					  TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(TimeOfTheGame)), // The change is in this line
					  TimeUnit.MILLISECONDS.toSeconds(TimeOfTheGame) - 
					  TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(TimeOfTheGame))); 

			  text(s, torroidX, 0.7f*histChartR); 
			  // ===============================================
			  		// -- get EEG index (one from A/M/S/P)
//			  indexLocalPlayer = getEEG();
//			  indexNetworkPlayer = getEEGNetworkUser();		  
			  // -- indexRND calculated in getRndNormalDistribution();
			  		  
			  
			  
			  switch(MainActivity.toroidGameType){
			  		// =================================================
			    	// =================================================
				    case "visualizer":
				    	indexLocalPlayer[0] = getEEG();
//				    	indexLocalPlayer[0] = 0;

				    	DataCollection(indexLocalPlayer[0]);
				    	
				    	// -- Convert a number range to another range, maintaining ratio
//				    	NewValue = (((OldValue - OldMin) * (NewMax - NewMin)) / (OldMax - OldMin)) + NewMin;
//				    	torroidY = displayHeight - (int) ((((indexLocalPlayer[0] - 0) * ((Y_line + line_hight) - Y_line)) / (100 - 0)) + Y_line);
				    	torroidY = displayHeight - (int) (((((int)(Math.round(Algorithm.MovingAverage(histData1min))) - 0) * ((Y_line + line_hight) - Y_line)) / (100 - 0)) + Y_line);
				    	
//				    	text(indexLocalPlayer[0],  displayWidth/3,  torroidY); 
				    	// 0 = 222; 50 = 888; 100 = 1554
				    	
						// -- initial constant offset on Y axis
//						torroidY = displayHeight - 1*displayHeight/10;						
						playerID = 0; 
//						torroidX = displayWidth/4; 
						
						// -- display selected index
						textFont(f,displayHeight/20);                 
						fill(255);  
						text(MainActivity.UserControl,  torroidX-3*sphereR , displayHeight - 1*sphereR);

//						drawVerticalLine();
//						//displaySphere(indexLocalPlayer[playerID], torroidX, torroidY);
//						displaySphere((int)(Math.round(Algorithm.MovingAverage(histData1min))), torroidX, torroidY);
						displayToroidByID((int)(Math.round(Algorithm.MovingAverage(histData1min))),
								playerID, torroidX, torroidY);
//						displayToroidByID(indexLocalPlayer[playerID], playerID, torroidX, torroidY);
						
						
//						displayToroidByID(indexLocalPlayer[playerID], playerID, torroidX, torroidY - plPositionY[playerID]);
//						displayToroidByID(indexLocalPlayer[playerID], playerID, torroidX, torroidY - indexLocalPlayer[playerID]*10);

						// -- display average Index
//						textFont(f,displayHeight/40); fill(255); 
//						text("Moving Average",  displayWidth/11, displayHeight/15); 
//						text(" 1min : " + Math.round(Algorithm.MovingAverage(histData1min))+ " " + Algorithm.nonZeroLength(histData1min), 								
//								displayWidth/10  , displayHeight/10); 
//						text("10min: " + Math.round(Algorithm.MovingAverage(histData10min))+ " "  + Algorithm.nonZeroLength(histData10min),
//								displayWidth/10  , displayHeight/10 + displayHeight/20); 
						
//						text("20min: " + Math.round(Algorithm.MovingAverage(histData20min)),  displayWidth/10  , displayHeight/10 + 2*displayHeight/20); 
//						text("30min: " + Math.round(Algorithm.MovingAverage(histData30min)),  displayWidth/10  , displayHeight/10 + 3*displayHeight/20); 
//						
//					    	displayGameLevel();

//							  // -- display current score
////							  textFont(f,displayHeight/40); 	//-- Specify font to be used	
//							  textFont(createFont("Arial",40,true)); 	//-- Specify font to be used	
//							  fill(255);                        //-- Specify font color 
//							  text("Winner player with index " + WI,  displayWidth/3,  displayHeight/2); 
						
						// -- display graph and values for Moving Average				
//						  DataCollection(indexLocalPlayer);	
//						  displayEEGindexCircleRND();
						
//						  DataCollection(indexLocalPlayer[0]);
							
				    	break;
				    // =================================================
				    // =================================================
				    	
			  } 
			 
			// ===============================================	
//			  DataCollection(indexLocalPlayer);	
//			  displayGraphOfEEGindex();
			  
			  // -- display graphs
			  displayCircleGraph(movingAvgHistData, 0, "1s" , 0);
			  displayCircleGraph(movingAvgHistData, 1, "1m" ,  1*displayHeight/5);
			  displayCircleGraph(movingAvgHistData, 2, "10m" , 2*displayHeight/5);
			  displayCircleGraph(movingAvgHistData, 3, "20m" , 3*displayHeight/5);
			  displayCircleGraph(movingAvgHistData, 4, "30m" , 4*displayHeight/5);

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
	/*	void displayGraphOfEEGindex() {
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
			 
			  // -- draw text
//			  textFont(f,32);  fill(255);	text("100",10f, legend100+legendAdjY); 

			  // -- draw chart lines
			  stroke(170,170,170); strokeWeight(2f); line(legendAdjX, legend0, displayWidth-legendAdjX*0.1f, legend0);
			  stroke(170,170,170); strokeWeight(2f); line(legendAdjX, legend100, displayWidth-legendAdjX*0.1f, legend100);
			  stroke(170,170,170); strokeWeight(1f); line(legendAdjX, legend60, displayWidth-legendAdjX*0.1f, legend60);
			  stroke(170,170,170); strokeWeight(1f); line(legendAdjX, legend40, displayWidth-legendAdjX*0.1f, legend40);
			  		  
			  // -- draw rectangular box on chart
			  fill(170,170,170, 40); noStroke();
//			  rect(100f, legend60, displayWidth, legend40);
			  rect(legendAdjX, legend40, displayWidth-legendAdjX*1.1f, legend60-legend40);
			  
			  
		}*/
		
		/** display LocalPlayer EEG index in  form of circle with R=index */
		void displayEEGindexCircleLocalPlayer() {
			  noSmooth();  
			  noFill();
			  beginShape();
//			  for (int i = 0; i < yvalues.length; i++) {
			  for (int i = LocalPlayerYvalues.length-1; i >= 0 ; i--) {
				  // -- setup width of the bars
				  strokeWeight(2);
				  
				  // -- setup colors of bars
				  if(LocalPlayerYvalues[i]<40){strokeWeight(4);}
				  if(LocalPlayerYvalues[i]>=40 && LocalPlayerYvalues[i]<=60){strokeWeight(3);}
				  if(LocalPlayerYvalues[i]>60){strokeWeight(2);}
				  
				  stroke(LocalPlayerColorsValues[i][0],LocalPlayerColorsValues[i][1],LocalPlayerColorsValues[i][2]);
				  
				  // -- setup width of the first 5 bars
				  if (i==LocalPlayerYvalues.length-1){strokeWeight(10); }	 
				  if (i==LocalPlayerYvalues.length-2){strokeWeight(8); }	  
				  if (i==LocalPlayerYvalues.length-3){strokeWeight(6); }	 
				  if (i==LocalPlayerYvalues.length-4){strokeWeight(5); }	 
				  if (i==LocalPlayerYvalues.length-5){strokeWeight(4); }	
				  
				  // -- draw bars
				  line(displayWidth-histChartR-histChartRsmall + (histChartRsmall)*sin(alpha),
						  histChartR+histChartRsmall + (histChartRsmall)*cos(alpha),
						  displayWidth-histChartR-histChartRsmall + ((histChartRsmall)+ LocalPlayerYvalues[i])*sin(alpha),
						  histChartR+histChartRsmall + ((histChartRsmall)+LocalPlayerYvalues[i])*cos(alpha));
				  // -- update alpha: shift bars to the left
				  alpha = alpha - (float)Math.PI/45f; 

			  }
			  endShape();
			  // -- reset alpha, to draw current value at angle=0
			  alpha = 0;
		}
		
			
		/** get EEG data from MainActivity and calculate S,P
		 *  moving average not working yet!!! */
		public int getEEG(){		
			if (millis() - ma_LastTime<=ma_length_ms){
			 	 switch(MainActivity.UserControl){
			 	 case "A":
			 		ma_value = 0.5f*(ma_value + eegService.At); 	
			 	 case "M":
			 		ma_value = 0.5f*(ma_value + eegService.Med); 
			 	 case "S":
			 		ma_value = 0.5f*(ma_value + (eegService.At - eegService.Med)); 
			 	 case "P":
			 		ma_value = 0.5f*(ma_value + (eegService.At + eegService.Med));
			 	 }
				 return 0;
			}else{
			
			 	 switch(MainActivity.UserControl){
			 	 case "A":
			 		ma_value = 0.5f*(ma_value + eegService.At); 
			 		return eegService.At;
			 	 case "M":
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
		
			
		/** collect user indexes to array for displaying history */
		public void DataCollectionLocalPlayer(int ind){
	  		// -- collect indexes
			  if (millis() - DataCollectionLastTimeLocalPlayer < DataCollectionDelay_ms) return; 
			  else{		
				  	// -- setup gradient colors
//				  indexR = (255 * indexRND ) / 100; indexG = 0;  indexB = (255 * (100 - indexRND )) / 100 ;	
				  	// -- setup fixed 3 colors based on threshold
				  if(MainActivity.UserControl.equals("S")){
					  if(ind<-30){indexR = 0; indexG = 0; indexB = 255;} // -- blue
					  if(ind>=-30 && ind<=30){indexR = 0; indexG = 255; indexB = 0;} // -- green
					  if(ind>30){indexR = 255; indexG = 0; indexB = 0;} // -- red
				  }else{
					  if(ind<40){indexR = 0; indexG = 0; indexB = 255;} // -- blue
					  if(ind>=40 && ind<=60){indexR = 0; indexG = 255; indexB = 0;} // -- green
					  if(ind>60){indexR = 255; indexG = 0; indexB = 0;} // -- red
				  }
				 
				  
				  LocalPlayerColorsValues[LocalPlayerColorsValues.length-1][0] = indexR;
			 	  LocalPlayerColorsValues[LocalPlayerColorsValues.length-1][1] = indexG;
			 	  LocalPlayerColorsValues[LocalPlayerColorsValues.length-1][2] = indexB;
			 		 
				  switch(MainActivity.UserControl){
				  	 // -- store current index value
//				 	 case "A": LocalPlayerYvalues[LocalPlayerYvalues.length-1] = ind; break;
				 	 case "A": LocalPlayerYvalues[LocalPlayerYvalues.length-1] = (histChartR/100)*ind; break;
//				 	 case "M": LocalPlayerYvalues[LocalPlayerYvalues.length-1] = (histChartR/100)*ind; break;
//				 	 case "S": LocalPlayerYvalues[LocalPlayerYvalues.length-1] = (histChartR/100)*(ind+100)/2; break;
//				 	 case "P": LocalPlayerYvalues[LocalPlayerYvalues.length-1] = (histChartR/100)*(ind+100)/2; break;
				  }
				  
				  // -- shift array, to keep only last EEGfraphHistLength values
				  for (int j = 0; j < LocalPlayerYvalues.length-1; j++) {
					  LocalPlayerYvalues[j] = LocalPlayerYvalues[j+1];	
					  LocalPlayerColorsValues[j][0] = LocalPlayerColorsValues[j+1][0];
					  LocalPlayerColorsValues[j][1] = LocalPlayerColorsValues[j+1][1];
					  LocalPlayerColorsValues[j][2] = LocalPlayerColorsValues[j+1][2];
				  }
				  
				  DataCollectionLastTimeLocalPlayer=millis();	
				  			  
			  }
		}
			
		
		/** display toroid for local/local+network player */
		public void drawVerticalLine(){	
			stroke(255,0,0); fill(255,0,0);
			rect(X_line, Y_line, line_width,line_hight);
			
			stroke(0,255,0); fill(0,255,0);
			rect(X_line, Y_line, 
					line_width, 
					displayHeight - (int) ((((MainActivity.AttLevelWarning - 0) * ((Y_line + line_hight) - Y_line)) / (100 - 0)) + Y_line) - Y_line);
			
			stroke(255,165,0); fill(255,165,0);
			rect(X_line, displayHeight - (int) ((((MainActivity.AttLevelWarning - 0) * ((Y_line + line_hight) - Y_line)) / (100 - 0)) + Y_line),
					line_width, 
					-(displayHeight - (int) ((((MainActivity.AttLevelWarning - 0) * ((Y_line + line_hight) - Y_line)) / (100 - 0)) + Y_line)) + 
					(displayHeight - (int) ((((MainActivity.AttLevelCritical - 0) * ((Y_line + line_hight) - Y_line)) / (100 - 0)) + Y_line) ) );
			
								
		}
		
		/** display toroid for local/local+network player */
		public void displaySphere(int ind, float x, float y){
				// -- setup gradient colors
//			  indexR = (255 * ind) / 100; indexG = 0;  indexB = (255 * (100 - ind)) / 100 ;
			  	// -- setup fixed 3 colors based on threshold			
			  if(MainActivity.UserControl.equals("S")){
//				  if(ind<-30){indexR = 0; indexG = 0; indexB = 255;} // -- blue
//				  if(ind>=-30 && ind<=30){indexR = 0; indexG = 255; indexB = 0;} // -- green
//				  if(ind>30){indexR = 255; indexG = 0; indexB = 0;} // -- red
			  }else{ // -- for A or M
				  if(ind<MainActivity.AttLevelCritical){indexR = 255; indexG = 0; indexB = 0;} // -- red
				  if(ind>=MainActivity.AttLevelCritical && ind<=MainActivity.AttLevelWarning){indexR = 255; indexG = 165; indexB = 0;} // -- yellow
				  if(ind>MainActivity.AttLevelWarning){indexR = 0; indexG = 255; indexB = 0;} // -- green
			  }
		
			  // -- display info about player
			  textFont(f,displayHeight/40); 	//-- Specify font to be used
			  fill(255);                        //-- Specify font color 
//			  text(ind, x + displayWidth/25, y - displayHeight/25);
			  text(ind, x + displayWidth/13, y);
			  
			  
			  
			  // -- draw the sphere
			  noFill();
			  stroke(indexR, indexG, indexB);
//			  noFill();
			  pushMatrix();
//			  stroke(indexR, indexG, indexB);
//			  noFill();
//			  fill(indexR, indexG, indexB);
//			  lights();
			  translate(x, y);
			  sphere(sphereR);
			  popMatrix();
			  
		
			  
		}
		
		/** display toroid for local/local+network player */
		public void displayToroidByID(int ind, int plID, float x, float y){
				// -- setup gradient colors
			  indexR = (255 * ind) / 100; indexG = 0;  indexB = (255 * (100 - ind)) / 100 ;
			  	// -- setup fixed 3 colors based on threshold			
			  if(ind<MainActivity.AttLevelCritical){
				  indexR = 255; indexG = 0; indexB = 0;
				  if(music_play_flag.equals("playing")){startService(new Intent(MusicService.ACTION_STOP)); music_play_flag = "stop";};
			  } 
			  if(ind>=MainActivity.AttLevelCritical && ind<=MainActivity.AttLevelWarning){
				  indexR = 255; indexG = 255; indexB = 0;
				  if(music_play_flag.equals("playing")){startService(new Intent(MusicService.ACTION_STOP)); music_play_flag = "stop";};  
			  } 
			  if(ind>MainActivity.AttLevelWarning){
				  indexR = 0; indexG = 255; indexB = 0;
				  if(music_play_flag.equals("stop")){startService(new Intent(MusicService.ACTION_PLAY)); music_play_flag = "playing";};
			  } 
			  
			  
		  		// -- create dynamic time-series based on EEG index value
			  switch(MainActivity.UserControl){
				case "A":
				  plPositionY[plID] = Algorithm.CreateDynamic(ind, plPositionY[plID], 0, displayHeight - 1*displayHeight/10, 1.0f, 40, 60, 0);	
				  break;
				case "M":
				  plPositionY[plID] = Algorithm.CreateDynamic(ind, plPositionY[plID], 0, displayHeight - 1*displayHeight/10, 1.0f, 40, 60, 0);	
				  break;
				case "S":
				  plPositionY[plID] = Algorithm.StoDynamicMovement(ind, plPositionY[plID], 0, displayHeight - 1*displayHeight/10, 1.0f, -30, 30, 0);
				  break;
			  }
		
			  		// -- move, spin and draw toroid 	
			  pushMatrix();
				  translate(x, y);
				  rotateZ(frameCount*PI/170); rotateY(frameCount*PI/170); rotateX(frameCount*PI/170);
//				  rotateZ(0); rotateY(0); rotateX(90);//frameCount*PI/170
				  thoroid(0,0, indexR, indexG, indexB, true, ExternalToroidRadius-InternalToroidDelta, ExternalToroidRadius);
			  popMatrix();
			  			  
			  		// -- display info about player
			  textFont(f,displayHeight/40); 	//-- Specify font to be used
	//		  textFont(f); 	//-- Specify font to be used
			  fill(255);                        //-- Specify font color 
	//		  text(MainActivity.UserControl + " -> " + ind, x + displayWidth/25, y - displayHeight/25); 
			  text(ind, x - 3*ExternalToroidRadius, y); 
			  
			  
			  // -- display current score
			  textFont(f,displayHeight/50); 	//-- Specify font to be used	
			  fill(255);                        //-- Specify font color 
			  text("audio fedback: "+  music_play_flag,   torroidX+1*sphereR , displayHeight - 1*sphereR); 
			  
		}
		
	
		/** display game level at the bottom of the screen */
//		public void displayGameLevel(){
//			  noStroke();
//			  for(int i=0; i<MaxGameLevel; i++){
//				  		// -- set fill to red
//					fill(255,0,0); 
//						// -- Draw ellipse using CENTER mode
////					ellipse(1f*displayWidth/10 + i*displayWidth/10, displayHeight - 0.2f*displayHeight/10, 15, 15); 
//					ellipse(displayWidth/10 + i*displayWidth/MaxGameLevel, displayHeight - 0.2f*displayHeight/10, 15, 15); 
//					 
//		  	  }
//			  for(int i=0; i<GameLevel; i++){
//					fill(0,0,255);  // -- set fill to blue
//						// -- Draw ellipse using CENTER mode
////					ellipse(1f*displayWidth/10 + i*displayWidth/10, displayHeight - 0.2f*displayHeight/10, 20, 20); 
//					ellipse(displayWidth/10 + i*displayWidth/MaxGameLevel, displayHeight - 0.2f*displayHeight/10, 20, 20); 
//			  }
//		}

		/** collect user indexes to array */
		public void DataCollection(int ind){
	  		// -- collect indexes
			  if (millis() - DataCollectionLastTimeLocalPlayer < DataCollectionDelay_ms) return; 
			  else{		
				  switch(MainActivity.UserControl){
				  	 // -- store current index value
				  	 case "A": {
				  		histData1min[histData1min.length-1] = ind; 
				  		histData10min[histData10min.length-1] = ind; 
				  		histData20min[histData20min.length-1] = ind; 
				  		histData30min[histData30min.length-1] = ind; 
				  		 
				  		break;
				  	 }
				  	 case "M": {
				  		histData1min[histData1min.length-1] = ind; 
				  		histData10min[histData10min.length-1] = ind; 
				  		histData20min[histData20min.length-1] = ind; 
				  		histData30min[histData30min.length-1] = ind; 
				  		 
				  		break;
				  	 }
				  	 case "S": {
				  		histData1min[histData1min.length-1] = (ind+100)/2; 
				  		histData10min[histData10min.length-1] = (ind+100)/2; 
				  		histData20min[histData20min.length-1] = (ind+100)/2; 
				  		histData30min[histData30min.length-1] = (ind+100)/2; 
				  		 
				  		break;
				  	 }
				  	 
//				 	 case "A": LocalPlayerYvalues[LocalPlayerYvalues.length-1] = (histChartR/100)*ind; break;
//				 	 case "M": LocalPlayerYvalues[LocalPlayerYvalues.length-1] = (histChartR/100)*ind; break;
//				 	 case "S": LocalPlayerYvalues[LocalPlayerYvalues.length-1] = (histChartR/100)*(ind+100)/2; break;
//				 	 case "P": LocalPlayerYvalues[LocalPlayerYvalues.length-1] = (histChartR/100)*(ind+100)/2; break;
				  }
				  
				  if (millis() - DataCollection >= OneMin){
					  // -- adding new value to the end
					  int l = movingAvgHistData[0].length;
					  movingAvgHistData[0][l-1] = ind;
					  movingAvgHistData[1][l-1] = Algorithm.MovingAverage(histData1min);
					  movingAvgHistData[2][l-1] = Algorithm.MovingAverage(histData10min);
					  movingAvgHistData[3][l-1] = Algorithm.MovingAverage(histData20min);
					  movingAvgHistData[4][l-1] = Algorithm.MovingAverage(histData30min);
					  
					  // -- shift array, to keep only last EEGfraphHistLength values
					  for (int j = 0; j < histData1min.length-1; j++) { histData1min[j] = histData1min[j+1]; }
					  for (int j = 0; j < histData10min.length-1; j++) { histData10min[j] = histData10min[j+1]; }
					  for (int j = 0; j < histData20min.length-1; j++) { histData20min[j] = histData20min[j+1]; }
					  for (int j = 0; j < histData30min.length-1; j++) { histData30min[j] = histData30min[j+1]; }
					  
					  for (int j = 0; j < EEGfraphHistLength-1; j++) {
						  for (int i = 0; i < arrD; i++) {
							  movingAvgHistData[i][j] = movingAvgHistData[i][j+1];
						  }
					  }
					  DataCollection=millis();
				  } else {
					  int l = movingAvgHistData[0].length;
					  movingAvgHistData[0][l-1] = ind;
					  movingAvgHistData[1][l-1] = Algorithm.MovingAverage(histData1min);
					  for (int j = 0; j < histData1min.length-1; j++) { histData1min[j] = histData1min[j+1]; }
					  for (int j = 0; j < histData10min.length-1; j++) { histData10min[j] = histData10min[j+1]; }
					  for (int j = 0; j < histData20min.length-1; j++) { histData20min[j] = histData20min[j+1]; }
					  for (int j = 0; j < histData30min.length-1; j++) { histData30min[j] = histData30min[j+1]; }
					  
					  for (int j = 0; j < EEGfraphHistLength-1; j++) {
						  for (int i = 0; i < 2; i++) {
							  movingAvgHistData[i][j] = movingAvgHistData[i][j+1];
						  }
					  }
					  
				  }
				  
				  DataCollectionLastTimeLocalPlayer=millis();	
				  		
//				  // -- display graphs
//				  displayCircleGraph(movingAvgHistData, 0);
			  }
		}
		
		/** display circle graph with R, color define by EEG index */
		void displayCircleGraph(float[][] graphData, int k, String s, float yAdj) {
			  noSmooth();  noFill();		  
			  
			  beginShape();			  
			  for (int i = graphData[0].length-1; i >= 0 ; i--) {
//			  for (int i = EEGfraphHistLength-1; i >= 0 ; i--) {
				  // -- setup width of the bars
				  strokeWeight(2);
				  
				  // -- setup colors of bars
				  if(graphData[k][i]<MainActivity.AttLevelCritical){strokeWeight(4); stroke(255,0,0);}
				  if(graphData[k][i]>=MainActivity.AttLevelCritical && graphData[k][i]<=MainActivity.AttLevelWarning){strokeWeight(3); stroke(255,165,0);}
				  if(graphData[k][i]>MainActivity.AttLevelWarning){strokeWeight(2); stroke(0,255,0);}
				  
//				  if(ind<MainActivity.AttLevelCritical){indexR = 255; indexG = 0; indexB = 0;} // -- red
//				  if(ind>=MainActivity.AttLevelCritical && ind<=MainActivity.AttLevelWarning){indexR = 255; indexG = 165; indexB = 0;} // -- yellow
//				  if(ind>MainActivity.AttLevelWarning){indexR = 0; indexG = 255; indexB = 0;} // -- green
//				  stroke(LocalPlayerColorsValues[i][0],LocalPlayerColorsValues[i][1],LocalPlayerColorsValues[i][2]);
				  
				  // -- setup width of the first 5 bars
				  if (i==graphData[0].length-1){strokeWeight(10); }	 
				  if (i==graphData[0].length-2){strokeWeight(8); }	  
				  if (i==graphData[0].length-3){strokeWeight(6); }	 
				  if (i==graphData[0].length-4){strokeWeight(5); }	 
				  if (i==graphData[0].length-5){strokeWeight(4); }	
				  
				  // -- draw bars
				  line(displayWidth-histChartR-histChartRsmall + (histChartRsmall)*sin(alpha),
						  histChartR+histChartRsmall + (histChartRsmall)*cos(alpha) + yAdj,
						  displayWidth-histChartR-histChartRsmall + ((histChartRsmall)+ graphData[k][i])*sin(alpha),
						  histChartR+histChartRsmall + ((histChartRsmall)+graphData[k][i])*cos(alpha) + yAdj);
				  
				  // -- update alpha: shift bars to the left
				  alpha = alpha - (float)Math.PI/45f; 

			  }
			  endShape();
			  // -- reset alpha, to draw current value at angle=0
			  alpha = 0;

				// -- display info about player
			  textFont(f,displayHeight/60);  fill(255);    
			  text(Math.round(graphData[k][graphData[0].length-1]),
					  displayWidth-histChartR-1.55f*histChartRsmall, histChartR+1.4f*histChartRsmall + yAdj);
			  
			  text(s, displayWidth-histChartR-1.5f*histChartRsmall - (histChartR+histChartRsmall), histChartR+1.4f*histChartRsmall + yAdj);
		}
		
		
		public int sketchWidth() { return displayWidth; }
		public int sketchHeight() { return displayHeight; }
		public String sketchRenderer() { return P3D; }	
		
}


