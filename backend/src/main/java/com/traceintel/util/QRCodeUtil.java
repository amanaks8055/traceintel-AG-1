package com.traceintel.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public final class QRCodeUtil {

    private QRCodeUtil() {
    }

    public static byte[] generate(String text, int size) throws WriterException, IOException {
        BitMatrix matrix = new QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size);
        ByteArrayOutputStream image = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", image);
        return image.toByteArray();
    }
}
