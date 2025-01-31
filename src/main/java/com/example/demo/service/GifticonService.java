package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.springframework.web.multipart.MultipartFile;
import java.util.Base64;
import java.io.IOException;
import java.util.Random;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import java.io.ByteArrayOutputStream;

@Service
public class GifticonService {

    @Autowired
    private SpringTemplateEngine templateEngine;

    // 기존 메소드들...

    public String processTemplate(String templateName, Model model) {
        Context context = new Context();
        model.asMap().forEach(context::setVariable);
        return templateEngine.process(templateName, context);
    }

    public String convertImageToBase64(MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            byte[] imageBytes = file.getBytes();
            return Base64.getEncoder().encodeToString(imageBytes);
        }
        return "";
    }

    public String generateCode(String codeType, String value) {
        // 바코드나 QR코드 생성 로직
        return value;
    }

    // 바코드 번호 생성 메소드 추가
    public String generateBarcodeNumber() {
        Random random = new Random();
        StringBuilder barcode = new StringBuilder();
        
        // 4개의 4자리 숫자 그룹 생성 (예: 7531 2262 7272 4023)
        for (int i = 0; i < 4; i++) {
            if (i > 0) barcode.append(" ");
            barcode.append(String.format("%04d", random.nextInt(10000)));
        }
        
        return barcode.toString();
    }

    public String generateBarcodeAsBase64(BarcodeFormat format, String value) throws WriterException, IOException {
        Code128Writer barcodeWriter = new Code128Writer();
        BitMatrix bitMatrix = barcodeWriter.encode(value, format, 300, 150);
        
        // 바코드 이미지를 Base64로 변환
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(imageBytes);
    }
} 