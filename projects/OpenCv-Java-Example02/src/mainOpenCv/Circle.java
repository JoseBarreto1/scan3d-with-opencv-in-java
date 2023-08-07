package mainOpenCv;

import org.opencv.core.Point;

public class Circle {
	public double radius;
    public Point center;

    public Circle(Point center, float radius) {
        this.center = center;
        this.radius = radius;
    }
}
