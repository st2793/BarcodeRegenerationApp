package com.example.demo.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.oned.Code128Writer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.EnumMap;
import java.util.Map;

@Slf4j
@Service
public class BarcodeGeneratorService {
    
    public BufferedImage generateBarcode(String barcodeText, BarcodeFormat format) {
        try {
            QRCodeWriter multiFormatWriter = new QRCodeWriter();
            BitMatrix bitMatrix = multiFormatWriter.encode(
                barcodeText,
                format,
                300,  // width
                100   // height
            );
            
            return MatrixToImageWriter.toBufferedImage(bitMatrix);
        } catch (Exception e) {
            log.error("바코드 생성 중 오류 발생", e);
            throw new RuntimeException("바코드 생성 실패: " + e.getMessage());
        }
    }

    public String generateBarcodeBase64(String barcodeText, BarcodeFormat format) {
        try {
            BufferedImage barcodeImage = generateBarcode(barcodeText, format);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(barcodeImage, "PNG", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            log.error("바코드 Base64 변환 중 오류 발생", e);
            throw new RuntimeException("바코드 Base64 변환 실패: " + e.getMessage());
        }
    }

    public String generateBarcodeAsBase64(String barcodeText) {
        try {
            log.debug("바코드 생성 시작: {}", barcodeText);
            
            // 바코드 생성 설정
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.MARGIN, 0);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            // Code128Writer를 사용하여 바코드 생성
            Code128Writer barcodeWriter = new Code128Writer();
            BitMatrix bitMatrix = barcodeWriter.encode(
                barcodeText, 
                BarcodeFormat.CODE_128, // EAN-13 대신 CODE_128 사용
                300, // 너비
                150, // 높이
                hints
            );

            // 바코드 이미지를 Base64로 변환
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);
            String base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());
            
            log.debug("바코드 생성 완료");
            return base64Image;

        } catch (Exception e) {
            log.error("바코드 생성 중 오류 발생", e);
            throw new RuntimeException("바코드 생성 실패: " + e.getMessage());
        }
    }
}