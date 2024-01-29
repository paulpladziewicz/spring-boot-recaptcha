package com.paulpladziewicz.springbootrecaptcha;

import com.paulpladziewicz.springbootrecaptcha.dto.SubscribeApiResponse;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
public class SubscribeController {

    @Value("${recaptcha.secret}")
    private String recaptchaSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/subscribe")
    public SubscribeApiResponse subscribe(@RequestParam @NotNull(message = "Email was not provided.") @Email(message = "Please provide a valid email address.") String email, @RequestParam @NotNull(message = "Recaptcha token was not provided.") String recaptchaToken) {
        SubscribeApiResponse response = new SubscribeApiResponse();
        String recaptchaVerifyUrl = "https://www.google.com/recaptcha/api/siteverify";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("secret", recaptchaSecret);
        map.add("response", recaptchaToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {
            ResponseEntity<Map> recaptchaResponseEntity = restTemplate.postForEntity(recaptchaVerifyUrl, request, Map.class);
            Map recaptchaResponse = recaptchaResponseEntity.getBody();

            if (recaptchaResponse != null && (Boolean) recaptchaResponse.get("success")) {
                Double score = (Double) recaptchaResponse.get("score");
                if (score != null && score > 0.5) {
                    response.setSuccess(true);
                    response.setMessage("Subscription successful.");
                } else {
                    response.setSuccess(false);
                    response.setMessage("Failed reCAPTCHA verification.");
                }
            } else {
                response.setSuccess(false);
                response.setMessage("reCAPTCHA verification failed.");
            }
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Server error during reCAPTCHA verification.");
        }

        return response;
    }
}
