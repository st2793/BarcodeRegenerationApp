package com.example.demo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class BarcodeApiController {

    @PostMapping("/readBarcode")
    public ResponseEntity<Map<String, String>> readBarcode(@RequestParam("image") MultipartFile image) {
        Map<String, String> response = new HashMap<>();
        
        try {
            BufferedImage bufferedImage = ImageIO.read(image.getInputStream());
            
            LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            
            MultiFormatReader reader = new MultiFormatReader();
            Result result = reader.decode(bitmap);
            
            response.put("format", result.getBarcodeFormat().toString());
            response.put("value", result.getText());
            
            log.info("바코드 읽기 성공: {}", result.getText());
            return ResponseEntity.ok(response);
            
        } catch (NotFoundException e) {
            log.warn("바코드를 찾을 수 없음");
            response.put("error", "바코드를 찾을 수 없습니다.");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("바코드 처리 중 오류", e);
            response.put("error", "바코드 처리 중 오류가 발생했습니다.");
            return ResponseEntity.ok(response);
        }
    }
}
 