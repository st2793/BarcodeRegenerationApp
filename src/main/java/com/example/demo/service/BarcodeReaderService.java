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
                throw new RuntimeException("이미지를 읽을 수 없습니다.");
            }

            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            MultiFormatReader reader = new MultiFormatReader();

            Map<DecodeHintType, Object> hints = new HashMap<>();
            hints.put(DecodeHintType.POSSIBLE_FORMATS, Arrays.asList(
                    BarcodeFormat.EAN_13,
                    BarcodeFormat.CODE_128,
                    BarcodeFormat.EAN_8));
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");

            Result barcodeResult = reader.decode(bitmap, hints);
            String decodedText = barcodeResult.getText();
            BarcodeFormat format = barcodeResult.getBarcodeFormat();

            if (format == BarcodeFormat.EAN_13) {
                if (isValidKoreanBarcode(decodedText)) {
                    log.info("한국 EAN-13 바코드 확인됨: {}", decodedText);
                }
            }

            result.put("format", format.toString());
            result.put("value", decodedText);
            
            log.info("바코드 읽기 성공 - 형식: {}, 값: {}", format, decodedText);
            
        } catch (NotFoundException e) {
            log.warn("바코드를 찾을 수 없음");
            result.put("error", "바코드를 찾을 수 없습니다.");
        } catch (Exception e) {
            log.error("바코드 처리 중 오류", e);
            result.put("error", e.getMessage());
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

    public Map<String, String> readBarcodeFromFile(String imagePath) {
        Map<String, String> result = new HashMap<>();
        
        try {
            BufferedImage image = ImageIO.read(new File(imagePath));
            
            if (image == null) {
                throw new IOException("이미지를 읽을 수 없습니다.");
            }

            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            MultiFormatReader reader = new MultiFormatReader();

            Map<DecodeHintType, Object> hints = new HashMap<>();
            hints.put(DecodeHintType.POSSIBLE_FORMATS, Arrays.asList(
                    BarcodeFormat.EAN_13,
                    BarcodeFormat.CODE_128,
                    BarcodeFormat.EAN_8));
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");

            Result barcodeResult = reader.decode(bitmap, hints);
            String decodedText = barcodeResult.getText();
            BarcodeFormat format = barcodeResult.getBarcodeFormat();

            // EAN-13 바코드인 경우 한국 바코드 검증
            if (format == BarcodeFormat.EAN_13) {
                if (isValidKoreanBarcode(decodedText)) {
                    log.info("한국 EAN-13 바코드 확인됨: {}", decodedText);
                }
            }

            result.put("format", format.toString());
            result.put("value", decodedText);
            
            log.info("바코드 읽기 성공 - 형식: {}, 값: {}", format, decodedText);
            
        } catch (NotFoundException e) {
            log.warn("바코드를 찾을 수 없음");
            result.put("error", "바코드를 찾을 수 없습니다.");
        } catch (Exception e) {
            log.error("바코드 처리 중 오류", e);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    private boolean isValidKoreanBarcode(String barcode) {
        if (barcode == null || barcode.length() != 13) {
            return false;
        }

        // 한국 국가 코드(880) 체크
        if (!barcode.startsWith("880")) {
            return false;
        }

        // 숫자만 포함하는지 체크
        if (!barcode.matches("\\d{13}")) {
            return false;
        }

        // 체크섬 검증
        try {
            int sum = 0;
            for (int i = 0; i < 12; i++) {
                int digit = Character.getNumericValue(barcode.charAt(i));
                sum += (i % 2 == 0) ? digit : digit * 3;
            }
            int expectedChecksum = (10 - (sum % 10)) % 10;
            int actualChecksum = Character.getNumericValue(barcode.charAt(12));

            return expectedChecksum == actualChecksum;
        } catch (Exception e) {
            log.error("체크섬 계산 중 오류", e);
            return false;
        }
    }
} 