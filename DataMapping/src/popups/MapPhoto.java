package popups;

import de.fhpotsdam.unfolding.UnfoldingMap;
import processing.core.PApplet;
import processing.core.PImage;

public class MapPhoto extends MapPopups{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String subtype = "photo";
	private MapPopups prevInt;
	PImage img;

	public MapPhoto(UnfoldingMap m, PApplet p, MapPopups i){
		super(m, p, i.getMarker());
		this.prevInt = i;
		setQuadrant(i.getQuadrant());
		setIntWidth(i.getIntWidth());
		setIntX(i.getIntX());
		setImage();
		setIntHeight(img.height);
		calcIntY();
	}
	
	@Override
	public String getSubType(){
		return this.subtype;
	}
	
	@Override
	public void drawPop(){
		p.image(getImage(), getIntX(), getIntY());
	}
	
	public int[] getImageDims(){
		int[] imgDim = new int[2];
		imgDim[0] = (int) getIntWidth();
		imgDim[1] = 0;
		return imgDim;
	}
	
	public void setImage(){
		try {
			String ext = getStringProp("filepath").split("\\.")[5].toString().toLowerCase();
			this.img = loadImage(getStringProp("filepath"), ext);
			this.img.resize(getImageDims()[0], getImageDims()[1]);
		} catch (Exception e) {
			this.img = loadImage("defaultPhoto.png", "png");
			this.img.resize(getImageDims()[0], getImageDims()[1]);
		}

	}
	
	public PImage getImage(){
		return this.img;
	}
	
	public void calcIntY(){
		if (getQuadrant()[1]){
			setIntY(prevInt.getIntY() - getIntHeight());
		} else {
			setIntY(prevInt.getIntY() + prevInt.getIntHeight());
		}
	}
	
}
