package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
public class Gifticon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String productName;
    private String detailMessage;
    private String originalImagePath;
    private String processedImagePath;
    private String qrCodePath;
    private String barcodePath;
    private String barcodeNumber;  // 바코드 숫자 저장
    private LocalDateTime expiryDate;
    private LocalDateTime createdAt;
    private String shareCode;
    private String productUrl;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
} 