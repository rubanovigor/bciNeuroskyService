package com.aiworker.bcineuroskyservice;

import java.util.Random;

public class Algorithm {	
	
	/** create dynamic time-series from rapid changing time-series 
	 * @return DynamicTS */
	public static float CreateDynamic(int TS, float DynamicTS,
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
	public static float StoDynamicMovement(int TS, float DynamicTS,
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
	
	/** convert S to dynamic movement*/
	public static float CircularMovement(float acceleration, float accMin, float accMax){
		float accelAlpha = (float) Math.toDegrees(acceleration);
		return accelAlpha;
	}

}
