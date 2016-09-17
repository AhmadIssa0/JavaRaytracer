
import java.io.*;
import java.awt.*;

public class BMPHandler {
	
	private static FileOutputStream fos;
	private static FileInputStream fis;
	
	// Changes buffer..
	private static int outputInt(FileOutputStream fos, byte buffer[], int index, int num) throws Exception {
		for (int i=0; i<4; i++) {
			buffer[index++] = (byte)(num%256);
			num = num >> 8;	
		}
		return index;
	}
	
	public static void saveBMP(String filename, Color colour[][]) {
		try {
		fos = new FileOutputStream(filename);
		int horiz=colour[0].length, vert=colour.length;
		byte[] buffer = new byte[54+horiz*vert*4];
		int index=0;
		buffer[index++] = (byte)'B';
		buffer[index++] = (byte)'M';
		index = outputInt(fos, buffer, index, 54+horiz*vert);
		index = outputInt(fos, buffer, index, 0);
		index = outputInt(fos, buffer, index, 54); // offset
		index = outputInt(fos, buffer, index, 40); // windows v3 format
		index = outputInt(fos, buffer, index, horiz);
		index = outputInt(fos, buffer, index, vert);
		buffer[index++] = 1;
		buffer[index++] = 0;
		buffer[index++] = 32;
		buffer[index++] = 0;
		index = outputInt(fos, buffer, index, 0);
		index = outputInt(fos, buffer, index, horiz*vert);
		index = outputInt(fos, buffer, index, horiz);
		index = outputInt(fos, buffer, index, vert);
		for (int i=0; i<2; i++) index = outputInt(fos, buffer, index, 0);
		for (int y=colour.length-1; y>=0; y--) {
			for (int x=0; x<colour[0].length; x++) {
					buffer[index++] = (byte)colour[y][x].getBlue();
					buffer[index++] = (byte)colour[y][x].getGreen();
					buffer[index++] = (byte)colour[y][x].getRed();
					buffer[index++] = (byte)colour[y][x].getAlpha();
			}	
		}
		fos.write(buffer);
		fos.close();
		} catch (Exception e) {
			e.printStackTrace();
			try { fos.close(); } catch (Exception p) {}
			System.exit(1);
		}
	}
	
	private static int inputInt() throws Exception {
		int num = 0;
		for (int i=0; i<4; i++) {
			num = num + (fis.read()<<8*i);
		}
		return num;
	}
	
	private static Color[][] toColourArray(byte[] contents, int horiz, int vert) {
		Color cols[][] = new Color[vert][horiz];
		for (int y=vert-1; y>=0; y--) {
			for (int x=0; x<horiz; x++) {
				int index = 4*((vert-y-1)*horiz + x);
				int blue=(int)contents[index], green=(int)contents[index+1];
				int red=(int)contents[index+2], alpha=(int)contents[index+3];
				if (blue < 0) blue = blue + 256;
				if (green < 0) green = green + 256;
				if (red < 0) red = red + 256;
				if (alpha < 0) alpha = alpha + 256;
				cols[y][x] = new Color(red, green, blue);
			}
		}
		return cols;
	}
	
	public static Color[][] readBMP(String filename) {
		try {
		fis = new FileInputStream(filename);
		fis.skip(10);
		int offset = inputInt();
		fis.skip(4);
		int horiz = inputInt(), vert = inputInt();
		fis.skip(offset - 26);
		byte[] contents = new byte[horiz*vert*4];
		fis.read(contents);
		Color cols[][] = toColourArray(contents, horiz, vert);
		fis.close();
		return cols;
		} catch (Exception e) {
			e.printStackTrace();
			try { fis.close(); } catch (Exception p) {}
			System.exit(1);	
		}
		return null;
	}
	
	private static Color averageColours(Color a, Color b) {
		return new Color(Math.min((a.getRed()+b.getRed()), 255), Math.min((a.getGreen()+b.getGreen()), 255),
						Math.min((a.getBlue()+b.getBlue()), 255), Math.min((a.getAlpha()+b.getAlpha()), 255));
	}
	
	
	public static Color[][] merge(String filename1, String filename2) {
		Color image1[][] = readBMP(filename1);
		Color image2[][] = readBMP(filename2);
		
		if (image1.length != image2.length || image1[0].length != image2[0].length) return null;
		Color image3[][] = new Color[image1.length][image1[0].length];
		for (int a=0; a<image1.length; a++)
		for (int b=0; b<image1[0].length; b++) {
			image3[a][b] = averageColours(image1[a][b], image2[a][b]);
		}
		return image3;
	}
	
	private static int difference(Color a, Color b) {
		return Math.abs(a.getRed() - b.getRed()) +
				Math.abs(a.getBlue() - b.getBlue()) +
				Math.abs(a.getGreen() - b.getGreen());
	}
	
	public static Color average(Color a[]) {
		int r=0,g=0,b=0;
		for (int i=0; i<a.length; i++) {
			r += a[i].getRed();
			g += a[i].getGreen();
			b += a[i].getBlue();
		}
		return new Color(r/a.length, g/a.length, b/a.length);
	}
	
	public static Color[][] antiAlias(Color[][] contents) {
		Color nc[][] = new Color[contents.length][contents[0].length];
		for (int i=0; i<nc.length; i++) for (int j=0; j<nc[0].length; j++) nc[i][j] = contents[i][j];
		for (int a=1; a<nc.length-1; a++) {
			for (int b=1; b<nc[0].length-1; b++) {
				int total = 0;
				total += difference(contents[a][b], contents[a-1][b-1]);
				total += difference(contents[a][b], contents[a-1][b]);
				total += difference(contents[a][b], contents[a][b-1]);
				
				total += difference(contents[a][b], contents[a+1][b+1]);
				total += difference(contents[a][b], contents[a+1][b]);
				total += difference(contents[a][b], contents[a][b+1]);
				
				total += difference(contents[a][b], contents[a-1][b+1]);
				total += difference(contents[a][b], contents[a+1][b-1]);
				
				if (total > 1200) {
					nc[a][b] = average((new Color[]{contents[a-1][b-1], contents[a-1][b],
													contents[a-1][b+1], contents[a+1][b+1],
													contents[a][b-1], contents[a][b],
													contents[a][b+1], contents[a+1][b]}));
				}
			}
		}
		return nc;
	}
	public static void main(String[] args) {
		saveBMP("C:/anti.bmp", antiAlias(readBMP("C:/mand.bmp")));
	}
}