package com.hendrikd.springaishowcase.controller;

import com.hendrikd.springaishowcase.service.RagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DescribeController {

    private final RagService ragService;

    public DescribeController(RagService ragService) {
        this.ragService = ragService;
    }

    /**
     * GET /describe
     * Describes the document via RAG (PGVector + OpenAI).
     */
    @GetMapping("/describe")
    public ResponseEntity<Map<String, String>> describe() {
        System.out.println(">>> [APP @ CONTROLLER] GET-Request triggered.");
        String description = ragService.describe();
        System.out.println(">>> [APP @ CONTROLLER] Sending answer.");
        return ResponseEntity.ok(Map.of(
                "document", "TIOBE.pdf",
                "description", description
        ));
    }
}
