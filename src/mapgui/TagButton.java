package mapgui;

import processing.core.PApplet;

public class TagButton extends Button {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String text = "";
	private float fontSize = 12;
	private boolean activeArray;

	public TagButton(PApplet p, String t, float x, float y) {
		super(p);
		this.text = t;
		setButX(x);
		setButY(y);
		calcWidth();
		calcHeight();
	}
	
	public TagButton(PApplet p, String t, boolean a) {
		super(p);
		this.text = t;
		this.activeArray = a;
		calcWidth();
		calcHeight();
	}

	@Override
	public void drawButton(PApplet p) {
		p.fill(color(230, 230, 230));
		p.stroke(color(150, 150, 150));
		p.strokeWeight(1);
		p.rect(getButX(), getButY(), getButWidth(), getButHeight(), 2);
		p.fill(0);
		p.textSize(fontSize);
		p.textAlign(LEFT,BOTTOM);
		p.text(text, (float) (getButX() + 2.5), (float) (getButY() + 1.3 * fontSize));
	}

	@Override
	public void doAction() {
		if (activeArray){
			this.activeArray = false;
		} else {
			this.activeArray = true;
		}
		
	}
	
	private void calcWidth(){
		p.textSize(fontSize);
		float w = p.textWidth(this.text) + 5;
		setButWidth(w);
	}

	private void calcHeight(){
		float h = this.fontSize + 5;
		setButHeight(h);
	}
	
	public String getText(){
		return this.text;
	}
	
	public float getFontSize(){
		return this.fontSize;
	}
	
	public boolean getActiveArray(){
		return this.activeArray;
	}
}
