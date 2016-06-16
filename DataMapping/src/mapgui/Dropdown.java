package mapgui;

import processing.core.PApplet;

public abstract class Dropdown extends Button {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private boolean active;
	protected String[] items = null;
	protected int fontSize = 13;

	public Dropdown(PApplet p) {
		super(p);
	}
	
	public abstract void doTypeAction();
	protected abstract boolean inSubItem(float x, float y);

	@Override
	public void drawButton(PApplet p) {
		if (items != null && items.length > 0){
			if (inButton(p.mouseX, p.mouseY) || this.active){
				p.fill(100, 100, 125);
				p.stroke(100, 100, 125);
			} else {
				p.fill(25, 25, 50);
				p.stroke(25, 25, 50);
			}
			p.rect(getButX(), getButY(), getButWidth(), getButHeight());
			
			p.strokeWeight((float)1.5);
			p.stroke(255, 255, 255);
			float hsf = (float) 2.6;
			float vsf = (float) 2.3;
			p.line(getButX() + getButWidth()/hsf, getButY() + getButHeight()/vsf, getButX() + getButWidth()/2 + (float) 0, getButY() + getButHeight() - getButHeight()/vsf);
			p.line(getButX() + getButWidth() - getButWidth()/hsf, getButY() + getButHeight()/vsf, getButX() + getButWidth()/2 - (float) 0, getButY() + getButHeight() - getButHeight()/vsf);
			
			if (this.active){
				p.fill(100, 100, 125, 225);
				p.strokeWeight(0);
				p.rect(getButX() + getButWidth(), getButY(), getMaxTextWidth(items, fontSize) + 15, items.length * (10 + fontSize) + 7, 0, 2, 2, 0);
				
				float xline = getButX() + getButWidth() + 5;
				p.textAlign(LEFT,TOP);
				p.fill(255);
				p.stroke(255);
				p.textSize(fontSize);
				for (int i = 0; i < items.length; i++){
					p.text(items[i], xline, getButY() + 7 + (10 + fontSize) * i);
				}
			}
		}
	}
	
	@Override
	public boolean inButton(float x, float y){
		if (getButVisible() && items != null && items.length > 0){
			if (!getActive()){
				if (x > getButX() && x < getButX() + getButWidth() && y > getButY() && y < getButY() + getButHeight()){
					return true;
				}
				return false;
			} else {
				if (inSubItem(x, y)){
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void doAction() {
		doTypeAction();
	}
	
	public void setItems(String[] i){
		this.items = i;
	}
	
	public void setActive(boolean a){
		this.active = a;
	}
	
	public boolean getActive(){
		return this.active;
	}
}
