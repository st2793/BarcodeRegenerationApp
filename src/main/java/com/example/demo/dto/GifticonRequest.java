package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;

@Getter @Setter
public class GifticonRequest {
    private String receiver;
    private String productName;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;
    
    private String message;
    private MultipartFile giftImage;
    private MultipartFile productImage;
} 