package pe.edu.utec.marketing.ai;

import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AiConfig {

    @Bean
    @Primary
    ScriptGenerator scriptGenerator(@Value("${anthropic.api-key:}") String apiKey,
                                    @Value("${anthropic.model}") String model) {
        if (apiKey == null || apiKey.isBlank()) {
            return new TemplateScriptGenerator();
        }
        return new ClaudeScriptGenerator(AnthropicOkHttpClient.builder().apiKey(apiKey).build(), model);
    }

    @Bean
    TemplateScriptGenerator templateScriptGenerator() {
        return new TemplateScriptGenerator();
    }
}
