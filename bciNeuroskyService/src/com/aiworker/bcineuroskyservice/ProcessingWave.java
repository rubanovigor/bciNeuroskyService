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

	/**
	 * Sine Wave
	 * by Daniel Shiffman.  
	 * 
	 * Render a simple sine wave. 
	 */
	 
	int xspacing = 16;   // How far apart should each horizontal location be spaced
	int w;              // Width of entire wave

	float theta = 0.0f;  // Start angle at 0
	float amplitude = 75.0f;  // Height of wave
	float period = 500.0f;  // How many pixels before the wave repeats
	float dx;  // Value for incrementing X, a function of period and xspacing
	float[] yvalues;  // Using an array to store height values for the wave

	public void setup() {
	 
	  w = width+16;
	  dx = (TWO_PI / period) * xspacing;
	  yvalues = new float[w/xspacing];
	}

	public void draw() {
	  background(0);
	  calcWave();
	  renderWave();
	}

	public void calcWave() {
	  // Increment theta (try different values for 'angular velocity' here
	  theta += 0.02f;

	  // For every x value, calculate a y value with sine function
	  float x = theta;
	  //x = MainActivity.At;
	  for (int i = 0; i < yvalues.length; i++) {
	    yvalues[i] = sin(x)*amplitude;
	    //[i] = MainActivity.At;
	    //yvalues[i] = sin(MainActivity.At)*amplitude;
	    x+=dx;
	  }
	}

	public void renderWave() {
	  noStroke();
	  fill(255);
	  // A simple way to draw the wave with an ellipse at each location
	  for (int x = 0; x < yvalues.length; x++) {
	    ellipse(x*xspacing, height/2+yvalues[x], 16, 16);
	  }
	}


	  public int sketchWidth() { return 640; }
	  public int sketchHeight() { return 360; }



}