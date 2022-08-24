package de.kosmos_lab.platform.gesture.data;

public class Point
{
    public float x;
    public float y;       // point coordinates
    public int stroke;     // the stroke index to which this point belongs
    public int lookupX;
    public int lookupY;
    
    public Point(float x, float y, int stroke)
    {
        this.x = x;
        this.y = y;
        this.stroke = stroke;
        this.lookupX = 0;
        this.lookupY = 0;
    }
}
