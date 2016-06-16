package mapgui;

import processing.core.PApplet;

public abstract class Button extends PApplet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected PApplet p;
	private float butX;
	private float butY;
	private float butWidth;
	private float butHeight;
	private boolean butVisible = true;
	protected boolean butClicked = false;
	private String type = "";
	
	public Button(PApplet p){
		this.p =p;
	}
	
	public void draw(){
		if (butVisible){
			p.pushStyle();
			drawButton(p);
			p.popStyle();
		}
	}
	
	public abstract void drawButton(PApplet p);
	public abstract void doAction();
	
	public boolean inButton(float x, float y){
		if (butVisible){
			if (x > getButX() && x < getButX() + getButWidth() && y > getButY() && y < getButY() + getButHeight()){
				return true;
			}
		}
		return false;
	}
	
	public float getMaxTextWidth(String[] str, float fs){
		p.pushStyle();
		p.textSize(fs);
		float max = p.textWidth(str[0]);
		for (String s : str){
			if (p.textWidth(s) > max){
				max = p.textWidth(s);
			}
		}
		p.popStyle();
		return max;
	}
	
	public float getTextWidth(String str, float fs){
		p.pushStyle();
		p.textSize(fs);
		float max = p.textWidth(str);
		p.popStyle();
		return max;
	}
	
	//getters and setters
	public void setButX(float x){
		this.butX = x;
	}
	
	public float getButX(){
		return this.butX;
	}
	
	public void setButY(float y){
		this.butY = y;
	}
	
	public float getButY(){
		return this.butY;
	}
	
	public void setButWidth(float w){
		this.butWidth = w;
	}
	
	public float getButWidth(){
		return this.butWidth;
	}
	
	public void setButHeight(float h){
		this.butHeight = h;
	}
	
	public float getButHeight(){
		return this.butHeight;
	}
	
	public void setButVisible(boolean bv){
		this.butVisible = bv;
	}
	
	public void setButClicked(boolean c){
		this.butClicked = c;
	}
	
	public boolean getButClicked(){
		return this.butClicked;
	}
	
	public boolean getButVisible(){
		return this.butVisible;
	}
	
	public void setType(String ty){
		this.type = ty;
	}
	
	public String getType(){
		return this.type;
	}
}
