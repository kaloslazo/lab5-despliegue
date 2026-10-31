package pe.edu.utec.marketing.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import pe.edu.utec.marketing.media.MediaStorage;

// expone las fotos y videos generados en /media/** (Instagram los descarga desde aqui)
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final MediaStorage storage;

    public WebConfig(MediaStorage storage) {
        this.storage = storage;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/media/**")
                .addResourceLocations(storage.getRoot().toUri().toString());
    }
}
