package mapgui;

import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;

public class TopToolbar extends PApplet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	PApplet p;
	List<Button> buttons = new ArrayList<Button>();
	private float topTBX;
	private float topTBY;
	private float topTBWidth;
	private float topTBHeight;
	private float lineX;
	
	public TopToolbar (PApplet p){
		this.p = p;
		this.topTBX = 0;
		this.topTBY = 0;
		this.topTBWidth = p.width;
		this.topTBHeight = 50;
		this.lineX = 250;
	}
	
	public TopToolbar (PApplet p, float x, float y, float w, float h, float lx){
		this.p = p;
		this.topTBX = x;
		this.topTBY = y;
		this.topTBWidth = w;
		this.topTBHeight = h;
		this.lineX = lx;
	}
	
	public TopToolbar (PApplet p, List<Button> b, float x, float y, float w, float h, float lx){
		this.p = p;
		this.buttons = b;
		this.topTBX = x;
		this.topTBY = y;
		this.topTBWidth = w;
		this.topTBHeight = h;
		this.lineX = lx;
	}
	
	public void draw(){
		setUpTopToolbar();
	}
	
	private void setUpTopToolbar(){
		p.pushStyle();
		p.fill(150, 150, 250);
		p.strokeWeight(0);
		p.rect(topTBX, topTBY, topTBWidth, topTBHeight);
		p.fill(255, 255, 255);
		
		p.strokeWeight(1);
		p.stroke(255);
		p.line(lineX, 0, lineX, topTBHeight);
		p.line(0, 50, 250, 50);
		
		p.textSize(16);
		p.textAlign(LEFT, BASELINE);
		p.fill(255);
		p.text("data", topTBX + 10, topTBY + 35);
		float tw = p.textWidth("data");
		p.textSize(24);
		p.text("Mapping", topTBX + 10 + tw + 1, topTBY + 35);
		p.popStyle();
	}
	
	//getters and setters
	public float getTopTBX(){
		return this.topTBX;
	}
	
	public float getTopTBY(){
		return this.topTBY;
	}
	public float getTopTBWidth(){
		return this.topTBWidth;
	}
	public float getTopTBHeight(){
		return this.topTBHeight;
	}
}
