package com.example.qrcodelogin.controller;

import com.example.qrcodelogin.service.QRCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/qrcode")
@RequiredArgsConstructor
public class QRCodeController {

    private final QRCodeService qrCodeService;

    @GetMapping("/generate")
    public ResponseEntity<Map<String, String>> generateQRCode() {
        return ResponseEntity.ok(qrCodeService.generateQRCode());
    }

    @GetMapping("/check/{qrCodeId}")
    public ResponseEntity<Map<String, String>> checkQRCodeStatus(@PathVariable String qrCodeId) {
        return ResponseEntity.ok(qrCodeService.checkQRCodeStatus(qrCodeId));
    }

    @PostMapping("/scan/{qrCodeId}")
    public ResponseEntity<Void> scanQRCode(@PathVariable String qrCodeId, @RequestHeader("Authorization") String token) {
        qrCodeService.scanQRCode(qrCodeId, token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/confirm/{qrCodeId}")
    public ResponseEntity<Void> confirmLogin(@PathVariable String qrCodeId, @RequestHeader("Authorization") String token) {
        qrCodeService.confirmLogin(qrCodeId, token);
        return ResponseEntity.ok().build();
    }
} 