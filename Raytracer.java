

import java.awt.*;
import java.util.*;
import javax.imageio.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class Raytracer {
	double INFINITY = 1000000000.0;
	Objects obs = new Objects();
	Light light1 = new Light(new Point(4,-4,5), new Color(250,250,250));
	//new Light(new Point(5,0,2), new Color(250,250,250));
	Light light2 = new Light(new Point(-4,4,5), new Color(250,250,250));
	Light light3 = new Light(new Point(4,4,5), new Color(250,250,250));
	Light light4 = new Light(new Point(-4,-4,5), new Color(250,250,250));
	//Point eye = new Point(8,0,3);
	Point eye = new Point(0,0,4.5);
	Vector<Light> lights = new Vector<Light>();
	Color map[][];
	
	public static void main(String[] args) {
		for (int i=0; i<50; i++) {
			System.out.println(i);
			new Raytracer(50, i);
		}
	}
	
	public Color[][] loadImage(String filename) {
		try {
			BufferedImage img = ImageIO.read(new File(filename));
			
		Color map[][] = new Color[img.getWidth()][img.getHeight()];
		for (int i=0; i<img.getWidth(); i++) {
			for (int j=0; j<img.getHeight(); j++) {
				int c = img.getRGB(i,j);
				int  red = (c & 0x00ff0000) >> 16;
				int  green = (c & 0x0000ff00) >> 8;
				int  blue = c & 0x000000ff;
				map[i][j] = new Color(red, green, blue);
			}
		}
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);	
		}
		return null;
	}
	
	public Raytracer(int rotations, int kkk) {
		map = loadImage("map.jpg"); // load map array
		System.out.println("image loaded");
		//System.out.println(Point.reflect(new Point(-1,-1,0), new Point(1,0,0)));
		lights.add(light1);
		lights.add(light2);
		lights.add(light3);
		lights.add(light4);
		//lights.add(new Light(new Point(6.2,5.5,8.5), new Color(250,150,150)));
		Sphere rs = new Sphere(map, rotations, kkk);
		//Ray r = new Ray(new Point(3,0,0), new Point(-1,0,0.1));
		//System.out.println(s.intersect(r));
		rs.radius = 1;
		rs.center = new Point(0,0,1); rs.transparent = 0.0;
		obs.v.add(rs);
		
		obs.v.add(new Plane(new Point(0,0,0), new Point(0,0,1), rs));
		
		/*
		Sphere s2 = new Sphere(); s2.center = new Point(0,2,0.5);
		s2.radius = 1.5; s2.transparent = 0.0;
		s2.col = new Color(30,230,30);
		obs.v.add(s2);
		
		Sphere s3 = new Sphere(); s3.center = new Point(0,-2,0); s3.radius = 1.0;
		s3.col = new Color(30,30,200); s3.transparent = 0.0;
		obs.v.add(s3);
		
		Sphere s5 = new Sphere(); s5.center = new Point(0.0,-4.0,0); s5.radius = 1.0;
		s5.col = new Color(90,30,230); s5.transparent = 0.0;
		obs.v.add(s5);
		
		Sphere s6 = new Sphere(); s6.center = new Point(-5.0,0.0,5.0); s6.radius = 3.0;
		s6.col = new Color(200,200,200); s6.transparent = 0.0; s6.reflect = 0.7; s6.diffuse=0.5;
		obs.v.add(s6);
		
		Sphere s4 = new Sphere(); s4.center = new Point(0,0,-100001); s4.radius = 100000.0;
		s4.col = new Color(250,250,250); s4.reflect = 0.3; s4.diffuse = 0.5; s4.transparent = 0.0;
		obs.v.add(s4);
		//obs.v.add(new Triangle(new Point(0,-5,0), new Point(0,0,0), new Point(0,0,5)));
		obs.v.add(new Plane(new Point(0,0,-1), new Point(0,0,1)));
		
		/*
		Sphere s7 = new Sphere(); s7.center = new Point(0,5.5,1.5);
		s7.radius = 2.5;
		s7.col = new Color(155,155,155);
		obs.v.add(s7);
		/*
		Sphere s8 = new Sphere(); s8.center = new Point(-25.0,-5.0,20.0); s8.radius = 20.0;
		s8.reflect = 0.8; s8.diffuse = 0.2;
		s8.col = new Color(255,255,255);
		obs.v.add(s8);*/
		
		Color[][] cols = new Color[500][500];
		
		for (int i=0; i<cols[0].length; i++) {
			for (int j=0; j<cols.length; j++) {
				//Point screen = new Point( 7, ((double)i)/cols.length * 2.0 - 1, ((double)j)/cols.length * 2.0 - 1 + 2 );
				Point screen = new Point( ((double)i)/cols.length * 2.0 - 1, ((double)j)/cols.length * 2.0 - 1, 4 );
				Point direction = Point.sub(screen, eye);
				cols[cols.length-j-1][i] = compute(new Ray(eye, direction), 3, false);
			}
		}
		
		if (kkk < 10) {
			BMPHandler.saveBMP("C:\\animation\\ray0"+kkk+".bmp", BMPHandler.antiAlias(cols));
		} else {
			BMPHandler.saveBMP("C:\\animation\\ray"+kkk+".bmp", BMPHandler.antiAlias(cols));
		}
		//BMPHandler.saveBMP("C:\\ray.bmp", cols);
	}
	
	

	public Color compute(Ray r, int depth, boolean reflection) {
		double t = INFINITY;
		SceneObj nS = obs.v.get(0); // may want to change since slow
		
		for (int i=0; i<obs.v.size(); i++) {
			SceneObj s = obs.v.get(i);
			double[] inter = s.intersect(r);
			double smaller = SceneObj.smaller(inter);
			if (smaller >= -0.5 && smaller < t) {
				nS = s; t = smaller;	
			}
		}
		Color total = new Color(0,0,0);
		if (Math.abs(t-INFINITY) < 1e-3) {
			if (reflection) {
				return new Color(0,0,0);
			} else {
				return new Color(250,250,250);	
			}
		} else {
			Point p = r.resolve(t);
			
			for (int j = 0; j<lights.size(); j++) {
				Light light = lights.get(j);
			if (light.shining(p, obs)) {
				double distance = Point.length(Point.sub(light.center, p));
				
				double scale = Point.dot(nS.normal(p), Point.normalized(Point.sub(light.center, p)));
				if (scale < 0.0) scale = 0.0;
				Color cp = scale(nS.getColor(p), scale);
				
				total = addC(total, scale(
					scale(addC(scale(cp, 0.7*nS.diffuse()),scale(nS.getColor(p), 0.2)),
										light.c.getRed()/255.0, light.c.getGreen()/255.0, light.c.getBlue()/255.0),
					nS.diffuse()));
					
				//System.out.println(Point.dot(Point.normalized(Point.reflect(r.dir,nS.normal(p))), Point.normalized(Point.sub(light.center,p))));
				total = addC(total, scale(nS.getColor(p), nS.specular() * 
						Math.pow(0.5*(1.0+Point.dot(Point.normalized(Point.reflect(Point.neg(r.dir),nS.normal(p))), 
									Point.normalized(Point.sub(p,light.center)))),50)));
			} else {
				total = addC(total,scale(
					scale(scale(nS.getColor(p), 0.2),light.c.getRed()/255.0, light.c.getGreen()/255.0, light.c.getBlue()/255.0),
					nS.diffuse()));
			}
				if (depth != 0 && nS.reflect() != 0) {
					Point rp = Point.reflect(r.dir, nS.normal(p));
					Ray nr = new Ray(Point.add(p, Point.scale(rp, 0.001)), rp);
					total = addC(total,scale(compute(nr, depth - 1, true), nS.reflect()));
					
				}
				
				
				if (depth != 0 && nS.transparent() != 0) {
					Ray nr = new Ray(Point.add(p, Point.scale(r.dir, 0.001)), r.dir);
					total = addC(total,scale(compute(nr, depth-1, true), nS.transparent()));	
				}
			}
		}
		return total;
	}
	
	public Color addC(Color c1, Color c2) {
		return new Color(Math.min(c1.getRed()+c2.getRed(),255),
						Math.min(c1.getGreen()+c2.getGreen(),255),
						Math.min(c1.getBlue()+c2.getBlue(), 255));	
	}
	
	public Color pToC(Point p) {
		return new Color((int)p.x,(int)p.y,(int)p.z);	
	}
	
	public Color scale(Color c, double factor1, double factor2, double factor3) {
		return new Color(Math.min((int)(c.getRed()*factor1),255), 
						Math.min((int)(c.getGreen()*factor2),255), 
						Math.min((int)(c.getBlue()*factor3), 255));
	}
	
	public Color scale(Color c, double factor) {
		return scale(c,factor,factor,factor);
	}

}

abstract class SceneObj {
	public static double larger(double[] d) {
		if (d.length == 1) return d[0];
		if (d[0] >= -0.00001 && d[1] >= -0.00001) {
			return Math.max(d[0],d[1]);
		} else if (d[0] >= -0.00001) {
			return d[0];
		} else if (d[1] >= -0.00001) {
			return d[1];	
		}
		return -1;
	}
	
	public static double smaller(double[] d) {
		if (d.length == 1) return d[0];
		if (d[0] >= -0.00001 && d[1] >= -0.00001) {
			return Math.min(d[0],d[1]);
		} else if (d[0] >= -0.00001) {
			return d[0];
		} else if (d[1] >= -0.00001) {
			return d[1];	
		}
		return -1;
	}
	public abstract double[] intersect(Ray r);	
	public abstract Point normal(Point p);
	public abstract Color getColor(Point p);
	public abstract double transparent();
	public abstract double reflect();
	public abstract double diffuse();
	public abstract double specular();
}


/*
class Triangle extends Plane {
	Point a,b,c;
	double px1, py1, px2, py2;
	
	
	public Triangle(Point a, Point b, Point c) {
		super(a, Point.cross(Point.sub(b,a),Point.sub(c,a)));
		this.a=a;this.b=b;this.c=c;
		Point A = Point.sub(a,c);
		Point B = Point.sub(b,c);
		double max = Math.max(normal.x,Math.max(normal.y,normal.z));
		if (normal.x == max) {
			px1=A.y; py1=A.z; px2=B.y; py2=B.z;
		} else if (normal.y == max) {
			px1=A.x; py1=A.z; px2=B.x; py2=B.z;
		} else {
			px1=A.x; py1=A.y; px2=B.x; py2=B.y;
		}
	}
	
	public double[] intersect(Ray r) {
		double rt[] = super.intersect(r);
		if (rt.length==2) return rt;
		Point inter = r.resolve(rt[0]);
		Point P = Point.sub(inter,c);
		double cx=0.0, cy=0.0;
		double max = Math.max(normal.x,Math.max(normal.y,normal.z));
		if (normal.x == max) {
			cx=P.y; cy=P.z;
		} else if (normal.y == max) {
			cx=P.x; cy=P.z;
		} else {
			cx=P.x; cy=P.y;
		}
		double d = -px2* py1 + px1 *py2;
		if (Math.abs(d) < 1e-5) return new double[]{-1,-1};
		double a = -(cy *px2 - cx* py2)/d;
		if (a < 0) return new double[]{-1,-1};
		double b = -(-cy* px1 + cx* py1)/d;
		if (b < 0) return new double[]{-1,-1};
		if (a+b > 1) return new double[]{-1,-1};
		return rt;
	}
}*/

class Plane extends SceneObj {
	Point normal;
	Color col = new Color(150,100,0);
	double diffuse = 0.7;
	double reflect = 0.01;
	double transparent = 0.0;
	double specular = 0.01;
	double K;
	Point pt;
	Sphere rs;
		
	public Plane(Point pos, Point normal, Sphere rs) {
			this.normal = Point.normalized(normal);
			this.pt = pos;
			K = Point.dot(normal, pos);
			this.rs = rs;
	}
		
	public Point normal(Point p) {
		return normal;
	}
		
	public Color getColor(Point p) {
		double radsq = p.x*p.x+p.y*p.y;
		double sc = 1/(4.0+radsq);
		return rs.getColor(new Point(4*p.x*sc, 4*p.y*sc, 2*radsq*sc));
		//return col;
		//return new Color((int)(Math.random()*250),(int)(Math.random()*250),250);
	}
	
	public double[] intersect(Ray r) {
		double d = Point.dot(normal, r.dir);
		if (Math.abs(d) < 0.0000001) return new double[]{-1,-1};
		double rt = (K - Point.dot(normal, r.pos))/d;
		if (rt < 0) return new double[]{-1,-1};
		return new double[]{ rt };
	}
	
	public double diffuse() { return diffuse; }
	public double transparent() { return transparent; }
	public double reflect() { return reflect; }	
	public double specular() { return specular;	}
}


class Sphere extends SceneObj {
	
	Color col = new Color(250,50,50);
	double diffuse = 0.4;
	double reflect = 0.1;
	double transparent = 1.0;
	double radius = 1.0;
	double specular = 0.1;
	Point center = new Point(0,0,0.0);
	Color map[][];
	int kkk;
	int rotations;
	
	public double diffuse() { return diffuse; }
	public double transparent() { return transparent; }
	public double reflect() { return reflect; }	
	public double specular() { return specular;	}
	
	public Sphere(Color map[][], int rotations, int kkk) {
		this.map = map;	
		this.kkk = kkk;
		this.rotations = rotations;
		c1 = Math.cos(-2*Math.PI/rotations*kkk);
		s1 = Math.sin(-2*Math.PI/rotations*kkk);
	}
	
	public static double larger(double[] d) {
		if (d[0] >= -0.00001 && d[1] >= -0.00001) {
			return Math.max(d[0],d[1]);
		} else if (d[0] >= -0.00001) {
			return d[0];
		} else if (d[1] >= -0.00001) {
			return d[1];	
		}
		return -1;
	}
	
	public static double smaller(double[] d) {
		if (d[0] >= -0.00001 && d[1] >= -0.00001) {
			return Math.min(d[0],d[1]);
		} else if (d[0] >= -0.00001) {
			return d[0];
		} else if (d[1] >= -0.00001) {
			return d[1];	
		}
		return -1;
	}
	
	public Point normal(Point p) {
		return Point.scale(Point.sub(p,	center), 1/radius);
	}
	
	double c1;
	double s1;
	public Color getColor(Point p) {
		double yss = p.x - center.x;
		double xss = p.y - center.y;
		double zss = p.z - center.z;
		double ys = yss;
		double zs = xss*s1 + zss*c1;
		double xs = xss*c1 - zss*s1;
		double theta = Math.acos(zs/radius); // from 0 to pi
		double phi = Math.atan2(ys,xs)+Math.PI;
		return map[map.length-1-Math.min((int)Math.round((phi/(2*Math.PI)*map.length)), map.length-1)] 
					[Math.min((int)(Math.round(theta/Math.PI*map[0].length)), map[0].length-1)]; ///////////////////////////////////
		//return col;	
	}
	
	
	public double[] intersect(Ray r) {
		// ||x - c||^2 = r^2
		// ray is s + t*d
		// v = s-c
		// -(v.d) +/- rt[(v.d)^2 - (v.v - r^2)]
		Point v = Point.sub(r.pos, center);
		Point d = r.dir;
		
		double i1 = Point.dot(v,d);
		double i2 = i1*i1 - (v.length()*v.length() - radius*radius);
		if (i2 < 0) return new double[]{-1.0,-1.0};
		double i3 = Math.sqrt(i2);
		
		double t1 = -i1 - i3;
		double t2 = -i1 + i3;
		
		return new double[]{t1,t2};
	}
	
}

class Objects {
	Vector<SceneObj> v = new Vector<SceneObj>();	
	
	class Intersection {
		double t;
		SceneObj s;	
		public Intersection(SceneObj s, double t) {
			this.s = s; this.t = t;	
		}
	}
	
	public Intersection intersection(Ray r) {
		double nT = 1000000.0;
		SceneObj nS = null;
		for (int i=0; i<v.size(); i++) {
			SceneObj s = v.get(i);
			double d = s.smaller(s.intersect(r));
			if (Math.abs(d) >= 0.0001 && d < nT) {
				nT = d;
				nS = s;	
			}
		}
		return new Intersection(nS, nT);
	}
}

class Light {
	Point center;
	Color c;
	
	public Light(Point center, Color c) {
		this.center = center; this.c = c;
	}
	
	public boolean shining(Point p, Objects obs) {
		Ray r = new Ray(p, Point.sub(center, p));
		for (int i=0; i<obs.v.size(); i++) {
			SceneObj s = obs.v.get(i);
			double smaller = s.smaller(s.intersect(r));
			//System.out.println(smaller);
			if (s.larger(s.intersect(r)) >= 0.001) return false;
			//if (Math.abs(s.intersect(r)) > 0.000001) return false;
		}
		return true;
	}
}

class Ray {
	Point dir;
	Point pos;
	
	public Ray(Point pos, Point dir) {
		this.dir = Point.normalized(dir);
		this.pos = pos;
	}
	
	public Point resolve(double t) {
		return Point.add(pos, Point.scale(dir,t));	
	}
	
}


class Point {
	double x, y, z;	
	public Point(double x, double y, double z) {
		this.x = x; this.y = y; this.z = z;	
	}
	
	public static Point normalized(double x, double y, double z) {
		return normalized(new Point(x,y,z));
	}
	
	public static Point normalized(Point p) {
		double x = p.x; double y=p.y; double z = p.z;
		if (x==0.0 && y==0.0 && z==0.0) return p;
		double r = 1/length(new Point(x,y,z));
		return scale(p,r);
	}
	
	// n must be unit!
	public static Point vres(Point dir, Point n) {
		return scale(n, dot(dir,n));
		
	}
	
	public static Point reflect(Point dir, Point n) {
		Point d = dir;
		return Point.sub(d, scale(vres(d,n), 2));	
	}
	
	public static double length(Point p) {
		return Math.sqrt(p.x*p.x + p.y*p.y + p.z*p.z);
	}
	
	public double length() {
		return length(this);
	}
	
	public static double dot(Point a, Point b) {
		return a.x*b.x + a.y*b.y + a.z*b.z;	
	}
	
	public static Point add(Point a, Point b) {
		return new Point(a.x+b.x,a.y+b.y,a.z+b.z);	
	}
	
	public static Point neg(Point a) {
		return scale(a,-1.0);	
	}
	
	public static Point sub(Point a, Point b) {
		return 	add(a,scale(b,-1.0));
	}
	
	public static Point scale(Point a, double s) {
		return new Point(a.x*s, a.y*s, a.z*s);	
	}
	
	public String toString() {
		return "("+this.x+", "+this.y+", " + this.z + ")";	
	}
	
	public static Point cross(Point a, Point b) {
		return new Point(a.y*b.z - b.y*a.z, b.x*a.z - a.x*b.z, a.x*b.y - b.x*a.y);	
	}
}