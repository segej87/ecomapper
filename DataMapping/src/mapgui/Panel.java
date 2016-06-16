package mapgui;

import processing.core.PApplet;

public abstract class Panel extends PApplet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private PApplet p;
	private float panX;
	private float panY;
	private float panWidth;
	private float panHeight;
	
	public Panel(PApplet p){
		this.p =p;
	}
	
	public void draw(){
		p.pushStyle();
		drawPanel(p);
		p.popStyle();
	}
	
	public abstract void drawPanel(PApplet p);
	
	public boolean inPanel(float x, float y){
		if (x > getPanX() && x < getPanX() + getPanWidth() && y > getPanY() && y < getPanY() + getPanHeight()){
			return true;
		}
		return false;
	}

	//getters and setters
		public void setPanX(float x){
			this.panX = x;
		}
		
		public float getPanX(){
			return this.panX;
		}
		
		public void setPanY(float y){
			this.panY = y;
		}
		
		public float getPanY(){
			return this.panY;
		}
		
		public void setPanWidth(float w){
			this.panWidth = w;
		}
		
		public float getPanWidth(){
			return this.panWidth;
		}
		
		public void setPanHeight(float h){
			this.panHeight = h;
		}
		
		public float getPanHeight(){
			return this.panHeight;
		}
}
