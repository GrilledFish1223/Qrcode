package com.ping.qr;

import java.awt.image.BufferedImage;
import jp.sourceforge.qrcode.data.QRCodeImage;
/**
 * @author: Zhangsp
 * @param:
 * @date: 2018/6/20 11:10
 */
public class QrcodeImage implements QRCodeImage {

    BufferedImage bufImg;

    public QrcodeImage(BufferedImage bufImg) {
        this.bufImg = bufImg;
    }

    public int getHeight() {
        return bufImg.getHeight();
    }

    public int getPixel(int x, int y) {
        return bufImg.getRGB(x, y);
    }

    public int getWidth() {
        return bufImg.getWidth();
    }

}