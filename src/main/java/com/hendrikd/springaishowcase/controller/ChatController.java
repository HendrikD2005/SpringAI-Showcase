package com.hendrikd.springaishowcase.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller for chat interactions with the AI model.
 */
@RestController
public class ChatController {

    private final ChatClient chatClient;

    private List<Media> images;

    /**
     * Constructor
     * <p>
     *     Provides the {@link ChatClient} interface with the {@link QuestionAnswerAdvisor} context.
     * </p>
     * @param builder {@link ChatClient} builder
     * @param vectorStore Vector store of the application
     */
    public ChatController(ChatClient.Builder builder, VectorStore vectorStore) {
        this.chatClient = builder
                .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore))
                .build();
        this.images = List.of(
                Media.builder().id("fruits").mimeType(MimeTypeUtils.IMAGE_PNG).data(new ClassPathResource("images/fruits.png")).build(),
                Media.builder().id("fruits-2").mimeType(MimeTypeUtils.IMAGE_PNG).data(new ClassPathResource("images/fruits-2.png")).build(),
                Media.builder().id("fruits-3").mimeType(MimeTypeUtils.IMAGE_PNG).data(new ClassPathResource("images/fruits-3.png")).build(),
                Media.builder().id("fruits-4").mimeType(MimeTypeUtils.IMAGE_PNG).data(new ClassPathResource("images/fruits-4.png")).build(),
                Media.builder().id("fruits-5").mimeType(MimeTypeUtils.IMAGE_PNG).data(new ClassPathResource("images/fruits-5.png")).build()
        );
    }

    /**
     * Image Description
     * <p>
     *     Asks the model to describe an image from the classpath.
     * </p>
     * @param image Image to describe
     * @return Image description
     */
    @GetMapping("/describe/{image}")
    String describeImage(@PathVariable String image) {
        Media media = Media.builder()
                .id(image)
                .mimeType(MimeTypeUtils.IMAGE_PNG)
                .data(new ClassPathResource("images/" + image + ".png"))
                .build();

        UserMessage userMessage = UserMessage.builder().text(
        """
        Explain what you see on the image. Explain very detailed.
        """).media(media).build();
        Prompt prompt = new Prompt(userMessage);

        return this.chatClient.prompt(prompt)
                .call()
                .content();
    }
}
