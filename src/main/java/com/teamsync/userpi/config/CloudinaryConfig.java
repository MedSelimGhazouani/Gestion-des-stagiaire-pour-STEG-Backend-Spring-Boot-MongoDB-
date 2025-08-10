package com.teamsync.userpi.config;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dyj6cdm7l",
                "api_key", "818155128313675",
                "api_secret", "jLPyPS0wRSDNWXRwTOIhrAOEirc",
                "secure", true
        ));
    }
}