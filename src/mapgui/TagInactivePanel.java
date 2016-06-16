package mapgui;

import processing.core.PApplet;

public class TagInactivePanel extends Panel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TagInactivePanel(PApplet p, float x, float y, float w, float h) {
		super(p);
		setPanX(x);
		setPanY(y);
		setPanWidth(w);
		setPanHeight(h);
	}

	@Override
	public void drawPanel(PApplet p) {
		//p.noFill();
		p.fill(color(50, 50, 75));
		p.stroke(255);
		p.strokeWeight(0);
		p.rect(getPanX(), getPanY(), getPanWidth(), getPanHeight());
		p.strokeWeight(1);
		p.line(getPanX(), getPanY(), getPanX() + getPanWidth(), getPanY());
		p.line(getPanX(), getPanY() + getPanHeight(), getPanX() + getPanWidth(), getPanY() + getPanHeight());
		p.textSize(13);
		p.fill(255, 255, 255);
		p.text("Inactive tags:", getPanX() + 10, getPanY() - 5);
	}

}
