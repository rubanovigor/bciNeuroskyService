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

import android.view.KeyEvent;


/** activity for Network Game Sample        */
public class ProcessingMultiUser extends PApplet{
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
			// -- variables to manipulate torroids colors dynamics
		int indexR, indexG, indexB; 
		int MedR; int MedG; int MedB;
		
		Random r = new Random(); private long mLastTime; int rndUpdDelay_ms = 1000;
		private long DataCollectionLastTime, DataCollectionLastTimeLocalPlayer;
		int DataCollectionDelay_ms = 1000;
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
		int EEGhistoryLength = 85;         							// -- length of array for collecting historical indexes 

		
		float[] LocalPlayerYvalues, RNDYvalues;  			// -- array for collecting hist data
		float[][] LocalPlayerColorsValues, RNDColorsValues; // -- array for collecting colors for hist data
		
		float alpha = 0f;
		
		PImage imgFinish; 
		
		
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
			 
			 f = createFont("Arial",16,true); // STEP 3 Create Font
			 DataCollectionLastTime = millis();
			 DataCollectionLastTimeLocalPlayer = millis();
			 CurrentTime = millis();
			 ma_LastTime = millis();
			 // -- for wave
//				 EEGhistoryLength = 90 ;
//				 xvalues = new float[EEGhistoryLength];
//				 xvalues[0] = 0;
//				 xvalues[1] = legendAdjX;
//				 
//				 for (int i = 2; i <EEGhistoryLength; i++) { 
//				   xvalues[i] = xvalues[i-1] + displayWidth/(EEGhistoryLength);
//				 }
				 
			 LocalPlayerYvalues = new float[EEGhistoryLength];
			 LocalPlayerColorsValues = new float[EEGhistoryLength][3];
						 
			 RNDYvalues = new float[EEGhistoryLength];
			 RNDColorsValues = new float[EEGhistoryLength][3];
				 
			 // -- finish line			 
			 imgFinish = loadImage("finish.png");
			 				 
		}

		public void draw(){
			  // -- draw background and setup basic lighting setup
			  background(0);
			  //lights();  // -- not working on some devices
		  
			  
			  // -- get index of single race winner
			  float largest = plPositionY[0]; int WinnerIndex = 0;
			  for (int i = 1; i < plPositionY.length; i++) {
			    if ( plPositionY[i] >= largest ) {
			        largest = plPositionY[i];
			        WinnerIndex = i;
			     }
			  }
			  		// -- update parameters when single race is finished
			  if ((displayHeight - 1*displayHeight/10 - ExternalToroidRadius-InternalToroidDelta - plPositionY[WinnerIndex])<=
					  (finishLineY1coordinate+finishLineY2coordinate)){				  
				  plScore[WinnerIndex] =  plScore[WinnerIndex] + 1;
				  Arrays.fill(plPositionY, 0f);
				  GameLevel = GameLevel + 1; 
				  CurrentTime=millis();
			  }
			  
			  		  
//			  if (GameLevel>MaxGameLevel)	{GameLevel = MaxGameLevel;} 
			  
			  // -- get index of single race winner
			  float l = plScore[0]; int WI = 0;
			  for (int i = 1; i < plScore.length; i++) {
			    if ( plScore[i] >= l ) {
			        l = plScore[i];
			        WI = i;
			     }
			  }
			  
			  // ===============================================
			  		// -- display finish line
//			  image(imgFinish, 0, 2.0f*displayHeight/10, displayWidth, 0.5f*displayHeight/10);
			  image(imgFinish, 0, finishLineY1coordinate, displayWidth, finishLineY2coordinate);
		  
//			  4/20 vs 1/20
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
			  
//			  text(s,4.5f*displayWidth/10, 2.0f*displayHeight/10 - 20f); 
			  text(s, 4.5f*displayWidth/10, 2*histChartR + 2*histChartRsmall); 
			  // ===============================================
			  		// -- get EEG index (one from att/med/S/P)
//			  indexLocalPlayer = getEEG();
//			  indexNetworkPlayer = getEEGNetworkUser();		  
			  // -- indexRND calculated in getRndNormalDistribution();
			  
			  switch(MainActivity.toroidGameType){
			  		// =================================================
			    	// =================================================
				    case "pl1 vs pl2":
				    	indexLocalPlayer[0] = getEEG();
				    	indexLocalPlayer[1] = getEEGPlayer2();
				    	indexLocalPlayer[2] = getEEGPlayer3();
				    	indexLocalPlayer[3] = getEEGPlayer4();
//				    	indexLocalPlayer[1] = 61;
//						indexNetworkPlayer = getEEGNetworkUser();
//						indexNetworkPlayer = 100;

						// -- initial constant offset on Y axis
						torroidY = displayHeight - 1*displayHeight/10;
						if(GameLevel <= MaxGameLevel){
							// -- draw 4 players in loop					
							for(int i = 0; i < 4; i = i+1) {							
								playerID = i; 
								torroidX = displayWidth/10 + playerID*displayWidth/4; 
								displayToroidByID(indexLocalPlayer[i], playerID, torroidX, torroidY - plPositionY[playerID]);
							}
							
													
	//						DataCollectionLocalPlayer(indexLocalPlayer);
	//						displayEEGindexCircleLocalPlayer();
							
								// -- networkUser
	//						Torroid2info = "player 2";
	//						displayNetworklPlayerToroid(displayWidth/2 - 3f*displayWidth/10, displayHeight - 1*displayHeight/10 - Player2Accel);
	//						DataCollectionRND(indexNetworkPlayer);
	//						displayEEGindexCircleRND();	
							
					    	displayGameLevel();
						}else{
							  // -- display current score
//							  textFont(f,displayHeight/40); 	//-- Specify font to be used	
							  textFont(createFont("Arial",40,true)); 	//-- Specify font to be used	
							  fill(255);                        //-- Specify font color 
							  text("Winner player with index " + WI,  displayWidth/3,  displayHeight/2); 
						}
				    	break;
				    // =================================================
				    // =================================================
				    	
//					case "rnd vs you":
//						indexLocalPlayer = getEEG();	 
////						indexLocalPlayer = -95;
////						indexRND = 100;
//						// -- indexRND calculated in getRndNormalDistribution();
//							// -- local
//						Torroid1info = "you";
//						displayToroidByID(displayWidth/2 + 2f*displayWidth/10,displayHeight - 1*displayHeight/10 - localPlayerAccel);
//						DataCollectionLocalPlayer(indexLocalPlayer);	displayEEGindexCircleLocalPlayer();
//							// -- RND
////						Torroid2info = "RND";
//						displayRND_Toroid(displayWidth/2 - 3f*displayWidth/10, displayHeight - 1*displayHeight/10 - Player2Accel);
//						DataCollectionRND(indexRND);	displayEEGindexCircleRND();						
//						
//						displayGameLevel();
//						break;
//						
//					case "rnd vs pl1 + pl2":
//						indexLocalPlayer = getEEG();
////						indexLocalPlayer = 39;
//						indexNetworkPlayer = getEEGNetworkUser();
////						indexNetworkPlayer = 100;
//						// -- calculate aggregate index
//						indexLocalPlayer = (indexLocalPlayer + indexNetworkPlayer)/2;
//							// -- local
//						Torroid1info = "team";
//						displayToroidByID(displayWidth/2 + 2f*displayWidth/10,displayHeight - 1*displayHeight/10 - localPlayerAccel);
//						DataCollectionLocalPlayer(indexLocalPlayer);	displayEEGindexCircleLocalPlayer();
//							// -- RND
////						Torroid2info = "RND";
//						displayRND_Toroid(displayWidth/2 - 3f*displayWidth/10, displayHeight - 1*displayHeight/10 - Player2Accel);
//						DataCollectionRND(indexRND);	displayEEGindexCircleRND();	
//						
//						displayGameLevel();
//						break;
			  } 
			 
			// ===============================================
//			  DataCollection(indexNetworkPlayer);	
//			  DataCollection(indexLocalPlayer);	
//			  DataCollectionRND();	displayEEGindexCircleRND();
			  
//			  displayGraphOfEEGindex();
			  


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
		
		/** display RND EEG index in  form of circle with R=index */
		void displayEEGindexCircleRND() {
			  noSmooth();  
			  noFill();
			  beginShape();
//			  for (int i = 0; i < yvalues.length; i++) {
			  for (int i = RNDYvalues.length-1; i >= 0 ; i--) {
				  // -- setup width of the bars
				  strokeWeight(2);
				  
				  // -- setup colors of bars
				  if(RNDYvalues[i]<40){strokeWeight(4);}
				  if(RNDYvalues[i]>=40 && RNDYvalues[i]<=60){strokeWeight(3);}
				  if(RNDYvalues[i]>60){strokeWeight(2);}
				  
				  stroke(RNDColorsValues[i][0],RNDColorsValues[i][1],RNDColorsValues[i][2]);
				  
				  // -- setup width of the first 5 bars
				  if (i==RNDYvalues.length-1){strokeWeight(10); }	 
				  if (i==RNDYvalues.length-2){strokeWeight(8); }	  
				  if (i==RNDYvalues.length-3){strokeWeight(6); }	 
				  if (i==RNDYvalues.length-4){strokeWeight(5); }	 
				  if (i==RNDYvalues.length-5){strokeWeight(4); }	
				  
				  // -- draw bars
				  line(histChartR+histChartRsmall + (histChartRsmall)*sin(alpha),
						  histChartR+histChartRsmall + (histChartRsmall)*cos(alpha),
						  histChartR+histChartRsmall + ((histChartRsmall)+ RNDYvalues[i])*sin(alpha),
						  histChartR+histChartRsmall + ((histChartRsmall)+RNDYvalues[i])*cos(alpha));
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
		public int getEEGPlayer2(){		
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
		
		/** get EEG data from MainActivity and calculate S,P
		 *  moving average not working yet!!! */
		public int getEEGPlayer3(){		
			if (millis() - ma_LastTime<=ma_length_ms){
			 	 switch(MainActivity.UserControl){
			 	 case "att":
			 		ma_value = 0.5f*(ma_value + eegService.At_pl3); 	
			 	 case "med":
			 		ma_value = 0.5f*(ma_value + eegService.Med_pl3); 
			 	 case "S":
			 		ma_value = 0.5f*(ma_value + (eegService.At_pl3 - eegService.Med_pl3)); 
			 	 case "P":
			 		ma_value = 0.5f*(ma_value + (eegService.At_pl3 + eegService.Med_pl3));
			 	 }
				 return 0;
			}else{
			
			 	 switch(MainActivity.UserControl){
			 	 case "att":
			 		ma_value = 0.5f*(ma_value + eegService.At_pl3); 
			 		return eegService.At_pl3;
			 	 case "med":
			 		ma_value = 0.5f*(ma_value + eegService.Med_pl3); 
			 		return eegService.Med_pl3;
			 	 case "S":
			 		ma_value = 0.5f*(ma_value + (eegService.At_pl3 - eegService.Med_pl3)); 
			 		return (eegService.At_pl3 - eegService.Med_pl3);
			 	 case "P":
			 		ma_value = 0.5f*(ma_value + (eegService.At_pl3 + eegService.Med_pl3));
			 		return (eegService.At_pl3 + eegService.Med_pl3);
			 	 default:
			 	     return 0;
			 	 }
			}
		}
		
		
		/** get EEG data from MainActivity and calculate S,P
		 *  moving average not working yet!!! */
		public int getEEGPlayer4(){		
			if (millis() - ma_LastTime<=ma_length_ms){
			 	 switch(MainActivity.UserControl){
			 	 case "att":
			 		ma_value = 0.5f*(ma_value + eegService.At_pl4); 	
			 	 case "med":
			 		ma_value = 0.5f*(ma_value + eegService.Med_pl4); 
			 	 case "S":
			 		ma_value = 0.5f*(ma_value + (eegService.At_pl4 - eegService.Med_pl4)); 
			 	 case "P":
			 		ma_value = 0.5f*(ma_value + (eegService.At_pl4 + eegService.Med_pl4));
			 	 }
				 return 0;
			}else{
			
			 	 switch(MainActivity.UserControl){
			 	 case "att":
			 		ma_value = 0.5f*(ma_value + eegService.At_pl4); 
			 		return eegService.At_pl4;
			 	 case "med":
			 		ma_value = 0.5f*(ma_value + eegService.Med_pl4); 
			 		return eegService.Med_pl4;
			 	 case "S":
			 		ma_value = 0.5f*(ma_value + (eegService.At_pl4 - eegService.Med_pl4)); 
			 		return (eegService.At_pl4 - eegService.Med_pl4);
			 	 case "P":
			 		ma_value = 0.5f*(ma_value + (eegService.At_pl4 + eegService.Med_pl4));
			 		return (eegService.At_pl4 + eegService.Med_pl4);
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
//				 	 case "att": LocalPlayerYvalues[LocalPlayerYvalues.length-1] = ind; break;
				 	 case "att": LocalPlayerYvalues[LocalPlayerYvalues.length-1] = (histChartR/100)*ind; break;
				 	 case "med": LocalPlayerYvalues[LocalPlayerYvalues.length-1] = (histChartR/100)*ind; break;
				 	 case "S": LocalPlayerYvalues[LocalPlayerYvalues.length-1] = (histChartR/100)*(ind+100)/2; break;
				 	 case "P": LocalPlayerYvalues[LocalPlayerYvalues.length-1] = (histChartR/100)*(ind+100)/2; break;
				  }
				  
				  // -- shift array, to keep only last EEGhistoryLength values
				  for (int j = 0; j < LocalPlayerYvalues.length-1; j++) {
					  LocalPlayerYvalues[j] = LocalPlayerYvalues[j+1];	
					  LocalPlayerColorsValues[j][0] = LocalPlayerColorsValues[j+1][0];
					  LocalPlayerColorsValues[j][1] = LocalPlayerColorsValues[j+1][1];
					  LocalPlayerColorsValues[j][2] = LocalPlayerColorsValues[j+1][2];
				  }
				  
				  DataCollectionLastTimeLocalPlayer=millis();	
				  			  
			  }
		}
			
		
		/** collect RND indexes to array for displaying */
		public void DataCollectionRND(int ind){
	  		// -- collect indexes
			  if (millis() - DataCollectionLastTime < DataCollectionDelay_ms) return; 
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
				  
				  RNDColorsValues[RNDColorsValues.length-1][0] = indexR;
			 	  RNDColorsValues[RNDColorsValues.length-1][1] = indexG;
			 	  RNDColorsValues[RNDColorsValues.length-1][2] = indexB;
			 		 
				  switch(MainActivity.UserControl){
				  	 // -- store current index value
//				 	 case "att": RNDYvalues[RNDYvalues.length-1] = ind; break;
				 	 case "att": RNDYvalues[RNDYvalues.length-1] = (histChartR/100)*ind; break;
				 	 case "med": RNDYvalues[RNDYvalues.length-1] = (histChartR/100)*ind; break;
				 	 case "S": RNDYvalues[RNDYvalues.length-1] = (histChartR/100)*(ind+100)/2; break;
				 	 case "P": RNDYvalues[RNDYvalues.length-1] = (histChartR/100)*(ind+100)/2; break;
				  }
				  
				  // -- shift array, to keep only last EEGhistoryLength values
				  for (int j = 0; j < RNDYvalues.length-1; j++) {
					  RNDYvalues[j] = RNDYvalues[j+1];	
					  RNDColorsValues[j][0] = RNDColorsValues[j+1][0];
					  RNDColorsValues[j][1] = RNDColorsValues[j+1][1];
					  RNDColorsValues[j][2] = RNDColorsValues[j+1][2];
				  }
				  
				  DataCollectionLastTime=millis();	
				  			  
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
				 indexRND  = (int) Math.round(val);
				 if (indexRND >100) {indexRND =100;} if (indexRND <0) {indexRND =0;}	           
//				 if (mLastTime>rndUpdDelay_ms){mLastTime=0;}
				 Torroid2info = "mean ~ " + String.valueOf(50+GameLevel*5);
			  	 break;
			 case "med":
				 val = r.nextGaussian() * 25 + (50+GameLevel*5); // 50 (mean); 25 (standard deviation) - 70% of data
				 indexRND  = (int) Math.round(val);
				 if (indexRND >100) {indexRND =100;} if (indexRND <0) {indexRND =0;}	           
//				 if (mLastTime>rndUpdDelay_ms){mLastTime=0;}
				 Torroid2info = "mean ~ " + String.valueOf(50+GameLevel*5);
				 break;
			 case "S":
				 val = r.nextGaussian() * 25 + (50+GameLevel*5); // 50 (mean); 25 (standard deviation) - 70% of data
				 indexRND  = (int) Math.round(val);
				 if (indexRND >100) {indexRND =100;} if (indexRND <0) {indexRND =0;}	
				 indexRND = indexRND*2 - 100; // adjust to interval -100:100 
//				 if (mLastTime>rndUpdDelay_ms){mLastTime=0;}
				 Torroid2info = "mean ~ " + String.valueOf(0+GameLevel*5);
				 break;
			}
			
		}
	
		
		/** display toroid for local/local+network player */
		public void displayToroidByID(int ind, int plID, float x, float y){
				// -- setup gradient colors
			  indexR = (255 * ind) / 100; indexG = 0;  indexB = (255 * (100 - ind)) / 100 ;
			  	// -- setup fixed 3 colors based on threshold			
			  if(MainActivity.UserControl.equals("S")){
				  if(ind<-30){indexR = 0; indexG = 0; indexB = 255;} // -- blue
				  if(ind>=-30 && ind<=30){indexR = 0; indexG = 255; indexB = 0;} // -- green
				  if(ind>30){indexR = 255; indexG = 0; indexB = 0;} // -- red
			  }else{ // -- for A or M
				  if(ind<40){indexR = 0; indexG = 0; indexB = 255;} // -- blue
				  if(ind>=40 && ind<=60){indexR = 0; indexG = 255; indexB = 0;} // -- green
				  if(ind>60){indexR = 255; indexG = 0; indexB = 0;} // -- red
			  }
			  
		  		// -- create dynamic time-series based on EEG index value
			  switch(MainActivity.UserControl){
				case "att":
				  plPositionY[plID] = Algorithm.CreateDynamic(ind, plPositionY[plID], 0, displayHeight - 1*displayHeight/10, 1.0f, 40, 60, 0);	
				  break;
				case "med":
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
				  thoroid(0,0, indexR, indexG, indexB, true, ExternalToroidRadius-InternalToroidDelta, ExternalToroidRadius);
			  popMatrix();
			  			
			  		// -- display info about player
			  if(plID==0){
				  textFont(f,displayHeight/70); 	//-- Specify font to be used	
				  fill(255);                        //-- Specify font color 
				  text(MainActivity.UserControl + " -> " + ind+"\n    (" + playerInfor[plID] + ")",
						  x + displayWidth/25, y - displayHeight/25); 
			  }else{
				  textFont(f,displayHeight/70); 	//-- Specify font to be used	
				  fill(255);                        //-- Specify font color 
				  text(MainActivity.UserControl + " -> " + ind+"\n    (" + playerInfor[plID] + ")",
						  x + displayWidth/25, y - displayHeight/25); 
			  }
			  
			  // -- display current score
			  textFont(f,displayHeight/40); 	//-- Specify font to be used	
			  fill(255);                        //-- Specify font color 
			  text(plScore[plID],  x ,  1.0f*displayHeight/10); 
			  
		}
		
		/** display toroid of the Network player */
		public void displayNetworklPlayerToroid(float x, float y){
				// -- setup gradient colors
			  indexR = (255 * indexNetworkPlayer) / 100; indexG = 0;  indexB = (255 * (100 - indexNetworkPlayer )) / 100 ;
			  	// -- setup fixed 3 colors based on threshold		  
			  if(MainActivity.UserControl.equals("S")){
				  if(indexNetworkPlayer<-30){indexR = 0; indexG = 0; indexB = 255;} // -- blue
				  if(indexNetworkPlayer>=-30 && indexNetworkPlayer<=30){indexR = 0; indexG = 255; indexB = 0;} // -- green
				  if(indexNetworkPlayer>30){indexR = 255; indexG = 0; indexB = 0;} // -- red
			  }else{
				  if(indexNetworkPlayer<40){indexR = 0; indexG = 0; indexB = 255;} // -- blue
				  if(indexNetworkPlayer>=40 && indexNetworkPlayer<=60){indexR = 0; indexG = 255; indexB = 0;} // -- green
				  if(indexNetworkPlayer>60){indexR = 255; indexG = 0; indexB = 0;} // -- red
			  }
			  
			  		// -- create dynamic ts based on pS
			  switch(MainActivity.UserControl){
				case "att":
				  Player2Accel = Algorithm.CreateDynamic(indexNetworkPlayer , Player2Accel, 0, displayHeight - 1*displayHeight/10, 1.0f, 40, 60, 0);	
				  break;
				case "med":
				  Player2Accel = Algorithm.CreateDynamic(indexNetworkPlayer , Player2Accel, 0, displayHeight - 1*displayHeight/10, 1.0f, 40, 60, 0);	
				  break;
				case "S":
				  Player2Accel = Algorithm.StoDynamicMovement(indexNetworkPlayer , Player2Accel, 0, displayHeight - 1*displayHeight/10, 1.0f, -30, 30, 0);
				  break;
			  }
			  	// -- center and spin toroid 
			  pushMatrix();
				  translate(x, y);
				  //			  rotateZ(0);		  rotateY(0);		  rotateX(0);
				  rotateZ(frameCount*PI/170); rotateY(frameCount*PI/170); rotateX(frameCount*PI/170);
				  thoroid(0,0, indexR, indexG, indexB, true, ExternalToroidRadius-InternalToroidDelta, ExternalToroidRadius);
			  popMatrix();
			
//			  textFont(f,32);  
			  textFont(f,displayHeight/50); 
			  fill(255);                   
			  text(MainActivity.UserControl + " -> " + indexNetworkPlayer+"\n    ("+Torroid2info+")",displayWidth/2 - 2.5f*displayWidth/10,displayHeight - 1.5f*displayHeight/10 - Player2Accel);
			  
		}
		
		/** display toroid of the rnd player */
		public void displayRND_Toroid(float x, float y){
			  getRndNormalDistribution();
			  	// -- setup gradient colors
//			  indexR = (255 * indexRND ) / 100; indexG = 0;  indexB = (255 * (100 - indexRND )) / 100 ;
			  	// -- setup fixed 3 colors based on threshold
			  if(MainActivity.UserControl.equals("S")){
				  if(indexRND<-30){indexR = 0; indexG = 0; indexB = 255;} // -- blue
				  if(indexRND>=-30 && indexRND<=30){indexR = 0; indexG = 255; indexB = 0;} // -- green
				  if(indexRND>30){indexR = 255; indexG = 0; indexB = 0;} // -- red
			  }else{
				  if(indexRND<40){indexR = 0; indexG = 0; indexB = 255;} // -- blue
				  if(indexRND>=40 && indexRND<=60){indexR = 0; indexG = 255; indexB = 0;} // -- green
				  if(indexRND>60){indexR = 255; indexG = 0; indexB = 0;} // -- red
			  }
			  
			  		// -- create dynamic ts based on pS
			  switch(MainActivity.UserControl){
				case "att":
				  Player2Accel = Algorithm.CreateDynamic(indexRND , Player2Accel, 0, displayHeight - 1*displayHeight/10, 1.0f, 40, 60, 0);	
				  break;
				case "med":
				  Player2Accel = Algorithm.CreateDynamic(indexRND , Player2Accel, 0, displayHeight - 1*displayHeight/10, 1.0f, 40, 60, 0);	
				  break;
				case "S":
				  Player2Accel = Algorithm.StoDynamicMovement(indexRND , Player2Accel, 0, displayHeight - 1*displayHeight/10, 1.0f, -30, 30, 0);
				  break;
			  }
			  	// -- center and spin toroid 
			  pushMatrix();
				  translate(x, y);
				  //			  rotateZ(0);		  rotateY(0);		  rotateX(0);
				  rotateZ(frameCount*PI/170); rotateY(frameCount*PI/170); rotateX(frameCount*PI/170);
				  thoroid(0,0, indexR, indexG, indexB, true, ExternalToroidRadius-InternalToroidDelta, ExternalToroidRadius);
			  popMatrix();
			
//			  textFont(f,32);  
			  textFont(f,displayHeight/50); 
			  fill(255);                   
			  text(MainActivity.UserControl + " -> " + indexRND+"\n    ("+ Torroid2info +")",displayWidth/2 - 2.5f*displayWidth/10,displayHeight - 1.5f*displayHeight/10 - Player2Accel);
			  
		}
		
		/** display game level at the bottom of the screen */
		public void displayGameLevel(){
			  noStroke();
			  for(int i=0; i<MaxGameLevel; i++){
				  		// -- set fill to red
					fill(255,0,0); 
						// -- Draw ellipse using CENTER mode
//					ellipse(1f*displayWidth/10 + i*displayWidth/10, displayHeight - 0.2f*displayHeight/10, 15, 15); 
					ellipse(displayWidth/10 + i*displayWidth/MaxGameLevel, displayHeight - 0.2f*displayHeight/10, 15, 15); 
					 
		  	  }
			  for(int i=0; i<GameLevel; i++){
					fill(0,0,255);  // -- set fill to blue
						// -- Draw ellipse using CENTER mode
//					ellipse(1f*displayWidth/10 + i*displayWidth/10, displayHeight - 0.2f*displayHeight/10, 20, 20); 
					ellipse(displayWidth/10 + i*displayWidth/MaxGameLevel, displayHeight - 0.2f*displayHeight/10, 20, 20); 
			  }
		}

		
		
		public int sketchWidth() { return displayWidth; }
		public int sketchHeight() { return displayHeight; }
		public String sketchRenderer() { return P3D; }	
		
}
