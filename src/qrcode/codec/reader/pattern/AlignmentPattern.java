package qrcode.codec.reader.pattern;

import jp.sourceforge.qrcode.codec.reader.*;
import jp.sourceforge.qrcode.codec.exception.AlignmentPatternEdgeNotFoundException;
import jp.sourceforge.qrcode.codec.geom.*;
import jp.sourceforge.qrcode.codec.util.*;

public class AlignmentPattern {
	static final int RIGHT = 1;
	static final int BOTTOM = 2;
	static final int LEFT = 3;
	static final int TOP = 4;

	static DebugCanvas canvas = DebugCanvas.getCanvas();
	Point[][] center;
	//int sqrtCenters; //�ʒu�����p�^�[����1�ӓ�����̐�
	int patternDistance;
	
	AlignmentPattern(Point[][] center, int patternDistance) {
		this.center = center;
		this.patternDistance = patternDistance;
	}
	
	public static AlignmentPattern findAlignmentPattern(boolean[][] image, FinderPattern finderPattern) 
		throws AlignmentPatternEdgeNotFoundException {

		Point[][] logicalCenters = getLogicalCenter(finderPattern);
		int logicalDistance = logicalCenters[1][0].getX() - logicalCenters[0][0].getX();

		//FinderPattern��AlignmentPattern�Ɠ��l�Ɉ������߂ɕϊ�����
		Point[][] centers = null;
		try {
			centers = getCenter(image, finderPattern, logicalCenters);
		} catch (AlignmentPatternEdgeNotFoundException e) {
			e.printStackTrace();
			throw e;
		}
		return new AlignmentPattern(centers, logicalDistance);

	}
	
	public Point[][] getCenter() {
		return center;
	}
	
	public int getLogicalDistance() {
		return patternDistance;
	}
	
	static Point[][] getCenter(boolean[][] image, FinderPattern finderPattern, Point[][] logicalCenters) 
			throws AlignmentPatternEdgeNotFoundException {
		int moduleSize = finderPattern.getModuleSize();
		int sin = finderPattern.getAngle()[0];
		int cos = finderPattern.getAngle()[1];

		Axis axis = new Axis(sin, cos, moduleSize);


		int sqrtCenters = logicalCenters.length;

		Point[][] centers = new Point[sqrtCenters][sqrtCenters];
		
		axis.setOrigin(finderPattern.getCenter(FinderPattern.UL));
		centers[0][0] = axis.translate(3, 3);
		//centers[0][0] = finderPattern.getCenter(FinderPattern.UL);
		axis.setOrigin(finderPattern.getCenter(FinderPattern.UR));
		centers[sqrtCenters - 1][0] = axis.translate(-3, 3);
		//centers[sqrtCenters - 1][0] = finderPattern.getCenter(FinderPattern.UR);
		axis.setOrigin(finderPattern.getCenter(FinderPattern.DL));
		centers[0][sqrtCenters - 1] = axis.translate(3, -3);
		//centers[0][sqrtCenters - 1] = finderPattern.getCenter(FinderPattern.DL);

		for (int y = 0; y < sqrtCenters; y++) {
			for (int x = 0; x < sqrtCenters; x++) {
				if (x == 1 && y == 0 && sqrtCenters == 3) { //�^��7�`13�̒�����̈ʒu�����p�^�[��
					centers[x][y] = Point.getCenter(centers[0][0], centers[sqrtCenters - 1][0]);
				}
				else if (x == 0 && y == 1 && sqrtCenters == 3) {//�^��7�`13�̍������̈ʒu�����p�^�[��
					centers[x][y] = Point.getCenter(centers[0][0], centers[0][sqrtCenters - 1]);					
				}
				else if (x >= 1 && y >= 1){

					Line[] additionalLines = { 
							new Line(centers[x - 1][y - 1], centers[x][y - 1]),
							new Line(centers[x - 1][y - 1], centers[x - 1][y])};
					int dx = centers[x - 1][y].getX() - centers[x - 1][y - 1].getX();
					int dy = centers[x - 1][y].getY() - centers[x - 1][y - 1].getY();
					additionalLines[0].translate(dx,dy);
					dx = centers[x][y - 1].getX() - centers[x - 1][y - 1].getX();
					dy = centers[x][y - 1].getY() - centers[x - 1][y - 1].getY();
					additionalLines[1].translate(dx,dy);
					centers[x][y] = Point.getCenter(additionalLines[0].getP2(), additionalLines[1].getP2());
				}
				else // dummy alignment pattern (source is finder pattern)
					continue;
				try {
					centers[x][y] = getPrecisionCenter(image, centers[x][y]);
				} catch (AlignmentPatternEdgeNotFoundException e) {
					e.printStackTrace();
					throw e;
				}
				canvas.drawCross(centers[x][y], Color.LIGHTRED);
			}
			//System.out.println("");
		}
		return centers;
	}

	
	static Point getPrecisionCenter(boolean[][] image, Point targetPoint) 
		throws AlignmentPatternEdgeNotFoundException {
		if (image[targetPoint.getX()][targetPoint.getY()] == QRCodeImageReader.POINT_LIGHT) {
			int scope = 0;
			boolean notFound = true;
			while (notFound) {
				scope++;
				for (int dy = scope; dy > -scope; dy--) {
					for (int dx = scope; dx > -scope; dx--) {
						if (image[targetPoint.getX() + dx][targetPoint.getY() + dy] == QRCodeImageReader.POINT_DARK) {
							targetPoint = new Point(targetPoint.getX() + dx,targetPoint.getY() + dy);
							notFound = false;
						}
					}
				}
			}
		}
		int x, lx, rx, y, uy, dy;
		x = lx = rx = targetPoint.getX();
		y = uy = dy = targetPoint.getY();

		while (!isEdge(image, lx, y, lx - 1, y) && lx >= 0)
			lx--;
		while (!isEdge(image, rx, y, rx + 1, y) && lx < image.length)
			rx++;
		while (!isEdge(image, x, uy, x, uy - 1) && uy >= 0)
			uy--;
		while (!isEdge(image, x, dy, x, dy + 1) && dy < image[0].length)
			dy++;
		
		return new Point((lx + rx + 1) / 2, (uy + dy + 1) / 2);
	}
	static boolean isEdge(boolean[][] image, int x, int y, int nx, int ny) {
		if (image[x][y] == QRCodeImageReader.POINT_LIGHT &&
			image[nx][ny] == QRCodeImageReader.POINT_DARK)
			return true;
		else
			return false;
	}
	
	//�e�ʒu�����p�^�[���̘_�����W�𓾂�
	static Point[][] getLogicalCenter(FinderPattern finderPattern) {
		int version = finderPattern.getVersion();
		Point[][] logicalCenters = new Point[1][1];
		int[] logicalSeeds = new int[1];

		//���΍��W�̌��ɂȂ����W,�s���W�̍쐬
		if (version == 1)
			return null;
		else if (version >= 2 && version <= 6) {
			logicalSeeds = new int[2];
			logicalSeeds[0] = 6;
			logicalSeeds[1] = 10 + 4 * version;
			logicalCenters = new Point[logicalSeeds.length][logicalSeeds.length];
		}
		else if (version >= 7 && version <= 13) {
			logicalSeeds = new int[3];
			logicalSeeds[0] = 6;
			logicalSeeds[1] = 8 + 2 * version;
			logicalSeeds[2] = 10 + 4 * version;
			logicalCenters = new Point[logicalSeeds.length][logicalSeeds.length];
		}
//		else if (version >= 14 && version <= 20) {
//			//canvas.println("logical center v14-20");
//			logicalSeeds = new int[4];
//			logicalSeeds[0] = 6;
//			logicalSeeds[1] = 26 + ((version - 14) / 3) * 4;
//			logicalSeeds[2] = 18 + 2 * version;
//			logicalSeeds[3] = 10 + 4 * version;
//			logicalCenters = new Point[logicalSeeds.length][logicalSeeds.length];
//		}
		
		//���ۂ̑��΍��W�̍쐬
		for (int col = 0; col < logicalCenters.length; col++) { //��@
			for (int row = 0; row < logicalCenters.length; row++) { //�s
				logicalCenters[row][col] = new Point(logicalSeeds[row], logicalSeeds[col]);
				//System.out.print(logicalCenters[row][col]);
			}
		}
		return logicalCenters;
		
	}
	
}
