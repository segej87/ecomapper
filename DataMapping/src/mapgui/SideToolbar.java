package mapgui;

import java.util.ArrayList;
import java.util.List;

import dataproc.Filtering;
import processing.core.PApplet;

public class SideToolbar extends PApplet{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	PApplet p;
	List<Button> buttons = new ArrayList<Button>();
	List<Panel> panels = new ArrayList<Panel>();
	List<TagButton> tbuttons = new ArrayList<TagButton>();
	private float sideTBX;
	private float sideTBY;
	private float sideTBWidth;
	private float sideTBHeight;
	private Filtering filter;
	
	//constructors	
	public SideToolbar (PApplet p, Filtering f, float x, float y, float w, float h){
		this.p = p;
		this.sideTBX = x;
		this.sideTBY = y;
		this.sideTBWidth = w;
		this.sideTBHeight = h;
		this.filter = f;
	}
	
	public SideToolbar (PApplet p, List<Button> b, float x, float y, float w, float h){
		this.p = p;
		this.buttons = b;
		this.sideTBX = x;
		this.sideTBY = y;
		this.sideTBWidth = w;
		this.sideTBHeight = h;
	}
	
	public void setup(){
		setUpSideToolbar();
		//loop to setup filter buttons
		String[] filterList = {"Access level","Geography","Datatype","Measured item"};
		float buffer = 15;
		float height = 30;
		float yPos = getSideTBY();
		for (int i = 0; i < 4; i++){
			FilterButton newBut = new FilterButton(p, filter, filterList[i], getSideTBX(), yPos + buffer, getSideTBWidth() - 35, height);
			addButton(newBut);
			addButton(new FilterDropdown(p, filter, filter.getAvailsByType(filterList[i]).toArray(new String[0]), newBut, newBut.getButX() + newBut.getButWidth(), newBut.getButY(), getSideTBWidth() - newBut.getButWidth(), newBut.getButHeight()));
			yPos += newBut.getButHeight() + buffer;
		}
		
		//set up panels
		TagActivePanel tap = new TagActivePanel(p, getSideTBX(), getButtons().get(getButtons().size() - 1).getButY() + getButtons().get(getButtons().size() - 1).getButHeight() + 40, getSideTBWidth(), 160);
		TagInactivePanel tip = new TagInactivePanel(p, getSideTBX(), tap.getPanY() + tap.getPanHeight() + 30, tap.getPanWidth(), tap.getPanHeight());
		addPanels(new Panel[] {tap, tip});
		
		String[] dateSegs = {"yyyy","MM","dd","HH","mm"};
		for (String s : new String[] {"START", "END"}){
			for (int i = 0; i < dateSegs.length; i++){
				Panel lastPanel = getPanels().get(getPanels().size() - 1);
				float yAdj = lastPanel.getPanY(); float xAdj = lastPanel.getPanX() + lastPanel.getPanWidth() + 15;
				if (i == 0) {yAdj = lastPanel.getPanY() + lastPanel.getPanHeight() + 20; xAdj = getSideTBX() + 50;}
				if (dateSegs[i].equals("HH")){xAdj = lastPanel.getPanX() + lastPanel.getPanWidth() + 20;}
				DatePanel ndp = new DatePanel(p, filter, s, dateSegs[i], xAdj, yAdj);
				addPanel(ndp);
				addButton(new ToggleButton(p, filter, ndp, xAdj + ndp.getPanWidth(), yAdj, 10, ndp.getPanHeight()));
			}
		}
		
		//set up tag buttons
		filter.setTagFilter(filter.getAvailTags());
		redrawTags();
	}
	
	public void draw(){
		p.pushStyle();
		setUpSideToolbar();
		for (Button b : buttons){
			b.draw();
		}
		calcTBPos();
		for (TagButton tb : tbuttons){
			tb.draw();
		}
		p.popStyle();
	}
	
	public void redrawTags(){
		resetTagButtons();
		for (String t : filter.getAvailTags()){
			TagButton tb = new TagButton(p, t, true);
			addTagButton(tb);
		}
		for (Button b : buttons){
			if (b.getType().equals("Measured item") || b.getType().equals("Measured itemDD")){
				if (filter.getAvailSpec() == null || filter.getAvailSpec().size() == 0){
					b.setButVisible(false);
				} else {b.setButVisible(true);}
			}
		}
	}
	
	private void calcTBPos() {
		Panel activePanel = null;
		Panel inactivePanel = null;
		for (Panel p : panels){
			if (p instanceof TagActivePanel){
				activePanel = p;
			}
			if (p instanceof TagInactivePanel){
				inactivePanel = p;
			}
		}
		if (activePanel != null){
			float lx = activePanel.getPanX() + 10;
			float ly = activePanel.getPanY() + 10;
			float w = activePanel.getPanWidth();
			for (TagButton tb : tbuttons){
				if (tb.getActiveArray()){
					if (lx + tb.getButWidth() + 5 < w){
						tb.setButX(lx);
						tb.setButY(ly);
						lx = lx + tb.getButWidth() + 5;
					} else {
						lx = panels.get(0).getPanX() + 10;
						ly = ly + tb.getFontSize()* 2;
						tb.setButX(lx);
						tb.setButY(ly);
						lx = lx + tb.getButWidth() + 5;
					}
				}
			}
		}
		
		
		if (inactivePanel != null){
			float lx = inactivePanel.getPanX() + 10;
			float ly = inactivePanel.getPanY() + 10;
			float w = inactivePanel.getPanWidth();
			for (TagButton tb : tbuttons){
				if (!tb.getActiveArray()){
					if (lx + tb.getButWidth() + 5 < w){
						tb.setButX(lx);
						tb.setButY(ly);
						lx = lx + tb.getButWidth() + 5;
					} else {
						lx = panels.get(0).getPanX() + 10;
						ly = ly + tb.getFontSize()* 2;
						tb.setButX(lx);
						tb.setButY(ly);
						lx = lx + tb.getButWidth() + 5;
					}
				}
			}
		}
		
	}

	private void setUpSideToolbar(){
		p.fill(25, 25, 50);
		p.strokeWeight(0);
		p.rect(sideTBX, sideTBY, sideTBWidth, sideTBHeight);
		for (Panel p : panels){
			p.draw();
		}
	}
	
	public void addButton(Button b){
		this.buttons.add(b);
	}
	
	public void addButtons(Button[] bl){
		for (Button b : bl){
			this.buttons.add(b);
		}
	}
	
	public void addTagButton(TagButton b){
		this.tbuttons.add(b);
	}
	
	public void addTagButtons(TagButton[] bl){
		for (TagButton b : bl){
			this.tbuttons.add(b);
		}
	}
	
	public void resetTagButtons(){
		this.tbuttons = new ArrayList<TagButton>();
	}
	
	public List<TagButton> getTagButtons(){
		return this.tbuttons;
	}
	
	public void addPanel(Panel p){
		this.panels.add(p);
	}
	
	public void addPanels(Panel[] pl){
		for (Panel p : pl){
			this.panels.add(p);
		}
	}
	
	//getters and setters
	public void setSideTBX(float x){
		this.sideTBX = x;
	}
	
	public float getSideTBX(){
		return this.sideTBX;
	}
	
	public void setSideTBY(float y){
		this.sideTBY = y;
	}
	
	public float getSideTBY(){
		return this.sideTBY;
	}
	
	public void setSideTBWidth(float w){
		this.sideTBWidth = w;
	}
	
	public float getSideTBWidth(){
		return this.sideTBWidth;
	}
	
	public void setSideTBHeight(float h){
		this.sideTBHeight = h;
	}
	
	public float getSideTBHeight(){
		return this.sideTBHeight;
	}
	
	public List<Button> getButtons(){
		return this.buttons;
	}
	
	public List<Panel> getPanels(){
		return this.panels;
	}

}
