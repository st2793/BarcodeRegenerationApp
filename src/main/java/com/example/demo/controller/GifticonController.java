package com.example.demo.controller;

import com.example.demo.dto.GifticonRequest;
import com.example.demo.service.BarcodeReaderService;
import com.example.demo.service.BarcodeGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Base64;
import java.util.Map;
import java.time.LocalDateTime;

// Swagger 관련 import 수정
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.example.demo.domain.Gifticon;
import com.example.demo.repository.GifticonRepository;
import com.google.zxing.BarcodeFormat;

@Slf4j
@Controller
@Tag(name = "기프티콘", description = "기프티콘 재생성 관련 API")
@RequiredArgsConstructor
public class GifticonController {
    
    private final GifticonRepository gifticonRepository;
    private final BarcodeReaderService barcodeReaderService;
    private final BarcodeGeneratorService barcodeGeneratorService;

    @Operation(summary = "메인 페이지", description = "기프티콘 재생성 메인 페이지를 반환합니다.")
    @GetMapping("/")
    public String home() {
        return "index";
    }

    @Operation(summary = "기프티콘 생성", description = "입력받은 정보로 기프티콘을 생성합니다.")
    @PostMapping("/share")
    public String createGifticon(@ModelAttribute GifticonRequest request, Model model) {
        try {
            // 기본 정보 설정
            model.addAttribute("receiver", request.getReceiver());
            model.addAttribute("productName", request.getProductName());
            
            // LocalDate를 LocalDateTime으로 변환
            LocalDateTime expiryDateTime = request.getExpiryDate().atStartOfDay();
            model.addAttribute("expiryDate", expiryDateTime);
            
            model.addAttribute("message", request.getMessage());

            // 이미지 처리 및 바코드 읽기
            if (request.getGiftImage() != null && !request.getGiftImage().isEmpty()) {
                Map<String, String> barcodeInfo = barcodeReaderService.readBarcodeFromImage(request.getGiftImage());
                
                if (!barcodeInfo.containsKey("error")) {
                    String format = barcodeInfo.get("format");
                    String value = barcodeInfo.get("value");
                    
                    model.addAttribute("barcodeFormat", format);
                    model.addAttribute("barcodeValue", value);
                    
                    // 바코드 이미지 생성
                    String barcodeImage = barcodeGeneratorService.generateBarcodeBase64(
                        value, 
                        BarcodeFormat.valueOf(format)
                    );
                    model.addAttribute("barcodeImage", barcodeImage);
                    
                    // Gifticon 엔티티 생성 및 저장
                    Gifticon gifticon = new Gifticon();
                    gifticon.setProductName(request.getProductName());
                    gifticon.setDetailMessage(request.getMessage());
                    gifticon.setExpiryDate(expiryDateTime);
                    gifticon.setBarcodeNumber(value);
                    gifticonRepository.save(gifticon);
                }
                
                // 이미지 Base64 인코딩
                byte[] imageBytes = request.getGiftImage().getBytes();
                String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                model.addAttribute("giftImage", base64Image);
            }

            if (request.getProductImage() != null && !request.getProductImage().isEmpty()) {
                byte[] imageBytes = request.getProductImage().getBytes();
                String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                model.addAttribute("productImage", base64Image);
            }

            return "share";
            
        } catch (Exception e) {
            log.error("기프티콘 처리 중 오류 발생: ", e);
            model.addAttribute("error", "기프티콘 처리 중 오류가 발생했습니다: " + e.getMessage());
            return "share";
        }
    }
} 