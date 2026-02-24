package com.java.dospring.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
 
import lombok.Data;
 
@Data
@Component
@ConfigurationProperties(prefix = "razorpay")
/**
 * RazorPayClientConfig.
 *
 * <p>Enterprise V4+ documentation block.
 */
public class RazorPayClientConfig {
    private String key;
    private String secret;
}
