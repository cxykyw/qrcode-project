package com.example.qrcodelogin.service;

import com.example.qrcodelogin.exception.QRCodeException;
import com.example.qrcodelogin.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class QRCodeService {

    private final StringRedisTemplate redisTemplate;
    private final JwtUtil jwtUtil;
    
    private static final String QR_CODE_PREFIX = "qrcode:";
    private static final String NONCE_PREFIX = "nonce:";
    private static final String STATUS_WAITING = "WAITING";
    private static final String STATUS_SCANNED = "SCANNED";
    private static final String STATUS_CONFIRMED = "CONFIRMED";

    public Map<String, String> generateQRCode() {
        String qrCodeId = UUID.randomUUID().toString();
        String key = QR_CODE_PREFIX + qrCodeId;
        
        log.info("Generating QR code with ID: {}", qrCodeId);
        
        // 存储二维码状态到Redis，设置5分钟过期
        redisTemplate.opsForValue().set(key, STATUS_WAITING, 5, TimeUnit.MINUTES);
        
        Map<String, String> response = new HashMap<>();
        response.put("qrCodeId", qrCodeId);
        response.put("status", STATUS_WAITING);
        return response;
    }

    public Map<String, String> checkQRCodeStatus(String qrCodeId) {
        String key = QR_CODE_PREFIX + qrCodeId;
        String status = redisTemplate.opsForValue().get(key);
        
        log.debug("Checking QR code status for ID: {}, status: {}", qrCodeId, status);
        
        Map<String, String> response = new HashMap<>();
        response.put("qrCodeId", qrCodeId);
        response.put("status", status != null ? status : "EXPIRED");
        
        // 如果状态是CONFIRMED，返回新的token
        if (STATUS_CONFIRMED.equals(status)) {
            String token = redisTemplate.opsForValue().get(key + ":token");
            response.put("token", token);
        }
        
        return response;
    }

    public void scanQRCode(String qrCodeId, String token) {
        log.info("Scanning QR code: {}", qrCodeId);
        
        // 从请求属性中获取用户名
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String username = (String) request.getAttribute("username");
        
        if (username == null) {
            log.warn("Username not found in request attributes");
            throw new SecurityException("Invalid token");
        }
        
        String key = QR_CODE_PREFIX + qrCodeId;
        String status = redisTemplate.opsForValue().get(key);
        
        if (status == null) {
            throw new QRCodeException("QR code expired or invalid");
        }
        
        if (STATUS_WAITING.equals(status)) {
            redisTemplate.opsForValue().set(key + ":username", username);
            redisTemplate.opsForValue().set(key, STATUS_SCANNED, 5, TimeUnit.MINUTES);
            log.info("QR code {} scanned by user: {}", qrCodeId, username);
        } else {
            log.warn("Invalid QR code status for scanning: {}", status);
            throw new QRCodeException("Invalid QR code status");
        }
    }

    public void confirmLogin(String qrCodeId, String token) {
        log.info("Confirming login for QR code: {}", qrCodeId);
        
        // 从请求属性中获取用户名
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String username = (String) request.getAttribute("username");
        
        if (username == null) {
            log.warn("Username not found in request attributes");
            throw new SecurityException("Invalid token");
        }
        
        String key = QR_CODE_PREFIX + qrCodeId;
        String status = redisTemplate.opsForValue().get(key);
        
        if (status == null) {
            throw new QRCodeException("QR code expired or invalid");
        }
        
        if (STATUS_SCANNED.equals(status)) {
            String storedUsername = redisTemplate.opsForValue().get(key + ":username");
            if (storedUsername == null) {
                throw new QRCodeException("User information not found");
            }
            
            // 验证确认登录的用户和扫描二维码的用户是否一致
            if (!username.equals(storedUsername)) {
                log.warn("User mismatch: scanner={}, confirmer={}", storedUsername, username);
                throw new SecurityException("Invalid user for confirmation");
            }
            
            // 生成新的token
            String newToken = jwtUtil.generateToken(username);
            
            // 存储token和更新状态
            redisTemplate.opsForValue().set(key + ":token", newToken, 5, TimeUnit.MINUTES);
            redisTemplate.opsForValue().set(key, STATUS_CONFIRMED, 5, TimeUnit.MINUTES);
            
            log.info("Login confirmed for user: {} with QR code: {}", username, qrCodeId);
        } else {
            log.warn("Invalid QR code status for confirmation: {}", status);
            throw new QRCodeException("Invalid QR code status");
        }
    }
} 