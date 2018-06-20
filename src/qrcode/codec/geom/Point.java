/*
 * �쐬���F 2004/09/13
 *
 * TODO ���̐������ꂽ�t�@�C���̃e���v���[�g��ύX����ɂ͎����Q�ƁB
 * �E�B���h�E �� �ݒ� �� Java �� �R�[�h�E�X�^�C�� �� �R�[�h�E�e���v���[�g
 */
package qrcode.codec.geom;


/**
 * @author Owner
 *
 * TODO ���̐������ꂽ�^�R�����g�̃e���v���[�g��ύX����ɂ͎����Q�ƁB
 * �E�B���h�E �� �ݒ� �� Java �� �R�[�h�E�X�^�C�� �� �R�[�h�E�e���v���[�g
 */
public class Point{
	public static final int RIGHT = 1;
	public static final int BOTTOM = 2;
	public static final int LEFT = 4;
	public static final int TOP = 8;
	
	int x;
	int y;
	
	public Point() {
		x = 0;
		y = 0;
	}
	public Point(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public void setY(int y) {
		this.y = y;
	}
	
	public void translate(int dx, int dy) {
		this.x += dx;
		this.y += dy;
	}
	
	public void set(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public String toString() {
		return "(" + Integer.toString(x) + "," + Integer.toString(y) + ")";
	}
	
	public static Point getCenter(Point p1, Point p2) {
		return new Point((p1.getX() + p2.getX()) / 2, (p1.getY() + p2.getY()) / 2);
	}
	
	public boolean equals(Point compare) {
		if (x == compare.x && y == compare.y) 
			return true;
		else
			return false;
	}
}
