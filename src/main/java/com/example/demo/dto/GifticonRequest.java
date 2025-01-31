package com.example.demo.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class GifticonRequest {
    private String receiver;
    private String productName;
    private String expiryDate;
    private String message;
    private MultipartFile giftImage;
    private MultipartFile productImage;
} 