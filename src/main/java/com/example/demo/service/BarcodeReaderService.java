package com.example.demo.service;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.oned.EAN13Writer;
import com.google.zxing.oned.EAN8Writer;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import java.awt.image.RescaleOp;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.EnumMap;
import java.util.Arrays;
import java.util.UUID;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.awt.image.Kernel;
import java.awt.image.ConvolveOp;

@Slf4j
@Service
public class BarcodeReaderService {
    
    @Value("${upload.path}")
    private String uploadDir;

    public Map<String, String> readBarcodeFromImage(MultipartFile file) {
        Map<String, String> result = new HashMap<>();
        
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            
            if (image == null) {
                result.put("error", "이미지를 읽을 수 없습니다.");
                return result;
            }

            // 바코드 읽기 설정
            Map<DecodeHintType, Object> hints = new HashMap<>();
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

            // 이미지를 바이너리 비트맵으로 변환
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            // 바코드 읽기 시도
            MultiFormatReader reader = new MultiFormatReader();
            Result barcodeResult = reader.decode(bitmap, hints);

            result.put("format", barcodeResult.getBarcodeFormat().toString());
            result.put("value", barcodeResult.getText());
            
            log.info("바코드 읽기 성공 - 형식: {}, 값: {}", 
                    barcodeResult.getBarcodeFormat(), barcodeResult.getText());
            
        } catch (NotFoundException e) {
            log.warn("바코드를 찾을 수 없음");
            result.put("error", "바코드를 찾을 수 없습니다.");
        } catch (Exception e) {
            log.error("바코드 처리 중 오류", e);
            result.put("error", "바코드 처리 중 오류가 발생했습니다.");
        }
        
        return result;
    }

    private BufferedImage preprocessImage(BufferedImage image) {
        // 이미지 크기 조정
        if (image.getWidth() > 1000 || image.getHeight() > 1000) {
            double scale = Math.min(1000.0 / image.getWidth(), 1000.0 / image.getHeight());
            int newWidth = (int) (image.getWidth() * scale);
            int newHeight = (int) (image.getHeight() * scale);
            BufferedImage resized = new BufferedImage(newWidth, newHeight, image.getType());
            Graphics2D g = resized.createGraphics();
            g.drawImage(image, 0, 0, newWidth, newHeight, null);
            g.dispose();
            image = resized;
        }

        // 이미지 선명도 향상
        BufferedImage sharpened = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        float[] sharpenMatrix = {
            0.0f, -1.0f, 0.0f,
            -1.0f, 5.0f, -1.0f,
            0.0f, -1.0f, 0.0f
        };
        Kernel kernel = new Kernel(3, 3, sharpenMatrix);
        ConvolveOp op = new ConvolveOp(kernel);
        op.filter(image, sharpened);

        return sharpened;
    }

    private BufferedImage rotateImage(BufferedImage image, int angle) {
        double rads = Math.toRadians(angle);
        double sin = Math.abs(Math.sin(rads));
        double cos = Math.abs(Math.cos(rads));
        int w = image.getWidth();
        int h = image.getHeight();
        int newWidth = (int) Math.floor(w * cos + h * sin);
        int newHeight = (int) Math.floor(h * cos + w * sin);

        BufferedImage rotated = new BufferedImage(newWidth, newHeight, image.getType());
        Graphics2D g2d = rotated.createGraphics();
        g2d.translate((newWidth - w) / 2, (newHeight - h) / 2);
        g2d.rotate(rads, w / 2, h / 2);
        g2d.drawRenderedImage(image, null);
        g2d.dispose();

        return rotated;
    }

    private String generateBarcode(BarcodeFormat format, String content) throws Exception {
        String fileName = "barcode_" + UUID.randomUUID() + ".png";
        Path barcodePath = Paths.get(uploadDir, fileName);
        
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.MARGIN, 0);
        
        BitMatrix bitMatrix;
        switch (format) {
            case EAN_13:
                EAN13Writer ean13Writer = new EAN13Writer();
                bitMatrix = ean13Writer.encode(content, BarcodeFormat.EAN_13, 300, 100, hints);
                break;
            case EAN_8:
                EAN8Writer ean8Writer = new EAN8Writer();
                bitMatrix = ean8Writer.encode(content, BarcodeFormat.EAN_8, 300, 100, hints);
                break;
            default:
                Code128Writer code128Writer = new Code128Writer();
                bitMatrix = code128Writer.encode(content, BarcodeFormat.CODE_128, 300, 100, hints);
        }
        
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", barcodePath);
        return fileName;
    }
} 