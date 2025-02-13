package org.doctech.cloud.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.doctech.cloud.service.CloudinaryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/sign")
public class UploadSignatureController {

    private final Cloudinary cloudinary;

    public UploadSignatureController(CloudinaryService cloudinaryService) {
        this.cloudinary = cloudinaryService.getCloudinary();
    }

    @GetMapping
    public Map<String, Object> generateSignature() {
        Map<String, Object> paramsToSign = ObjectUtils.asMap(
                "timestamp", System.currentTimeMillis() / 1000L,
                "upload_preset", "my_preset" // Optional: Use an upload preset
        );

        String signature = cloudinary.apiSignRequest(paramsToSign, cloudinary.config.apiSecret);

        paramsToSign.put("signature", signature);
        paramsToSign.put("api_key", cloudinary.config.apiKey);

        return paramsToSign;
    }
}
