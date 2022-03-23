package com.fisk.chartvisual.stringInterception;

/**
 * @author WangYan
 * @date 2022/3/1 16:16
 */
public class AcceptCanvasConfigInfo {

    private int type;
    private int width;
    private int height;
    private String color;
    private String image;
    public void setType(int type) {
        this.type = type;
    }
    public int getType() {
        return type;
    }

    public void setWidth(int width) {
        this.width = width;
    }
    public int getWidth() {
        return width;
    }

    public void setHeight(int height) {
        this.height = height;
    }
    public int getHeight() {
        return height;
    }

    public void setColor(String color) {
        this.color = color;
    }
    public String getColor() {
        return color;
    }

    public void setImage(String image) {
        this.image = image;
    }
    public String getImage() {
        return image;
    }

}
