package com.hendrikd.springaishowcase.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for chat interactions with the AI model.
 */
@RestController
public class ChatController {

    private final ChatClient chatClient;

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
    }

    /**
     * Basic Call
     * <p>
     *     Asks the model what the report says about the top 10 programming languages.
     * </p>
     *
     * @return answer from the AI model
     */
    @GetMapping("/")
    public String chat() {
        return chatClient.prompt()
                .user("What does the report say about the top 10 programming languages?")
                .call()
                .content();
    }


}
