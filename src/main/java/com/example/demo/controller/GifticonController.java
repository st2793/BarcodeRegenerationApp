package com.example.demo.controller;

import com.example.demo.dto.GifticonRequest;
import com.example.demo.service.BarcodeReaderService;
import com.example.demo.service.ImageStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Base64;
import java.util.Map;

// Swagger 관련 import 수정
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Slf4j
@Controller
@Tag(name = "기프티콘", description = "기프티콘 재생성 관련 API")
@RequiredArgsConstructor
public class GifticonController {
    
    private final BarcodeReaderService barcodeReaderService;
    private final ImageStorageService imageStorageService;

    @Operation(summary = "메인 페이지", description = "기프티콘 재생성 메인 페이지를 반환합니다.")
    @GetMapping("/")
    public String home() {
        return "index";
    }

    @Operation(summary = "기프티콘 생성", description = "입력받은 정보로 기프티콘을 생성합니다.")
    @PostMapping("/share")
    public String createGifticon(@ModelAttribute GifticonRequest request, Model model) {
        try {
            // 기본 정보 설정 (항상 설정)
            model.addAttribute("receiver", request.getReceiver());
            model.addAttribute("productName", request.getProductName());
            model.addAttribute("expiryDate", request.getExpiryDate());
            model.addAttribute("message", request.getMessage());

            // 이미지 처리 (기프티콘 이미지)
            if (request.getGiftImage() != null && !request.getGiftImage().isEmpty()) {
                byte[] imageBytes = request.getGiftImage().getBytes();
                String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                model.addAttribute("giftImage", base64Image);

                // 바코드 읽기 시도
                Map<String, String> barcodeInfo = barcodeReaderService.readBarcodeFromImage(request.getGiftImage());
                
                if (barcodeInfo.containsKey("error")) {
                    model.addAttribute("barcodeError", barcodeInfo.get("error"));
                } else {
                    model.addAttribute("barcodeValue", barcodeInfo.get("value"));
                    model.addAttribute("barcodeFormat", barcodeInfo.get("format"));
                }
            }

            // 상품 이미지 처리
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