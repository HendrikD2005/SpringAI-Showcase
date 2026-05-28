package com.hendrikd.springaishowcase.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagService {

    private static final String PDF_PATH = "TIOBE.pdf";
    private static final String DESCRIBE_QUERY =
            "Describe the whole document." + "What are the most important topics, aspects and data?";
    private static final Logger log = LoggerFactory.getLogger(RagService.class);

    private final VectorStore vectorStore;
    private final ChatClient chatClient;

    public RagService(VectorStore vectorStore, ChatClient.Builder chatClientBuilder) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClientBuilder.build(); // Use advisors, etc. here (optional)
    }

    /**
     * Ingests the document into the vector store.
     *
     * It loads the file, separates it into chunks and saves these chunks into the vector database (Vector Store).
     */
    @PostConstruct
    public void ingestDocument() {
        System.out.println(">>> [APP @ RagService: INGESTION] Ingesting document ...");

        // Step 1: Load the resource
        System.out.println(">>> [APP@RagService: INGESTION] Step 1/4 - Loading document ...");
        ClassPathResource resource = new ClassPathResource(PDF_PATH);

        if (!resource.exists()) {
            System.err.println(">>> [APP @ RagService: INGESTION] Step 1/4 - Failed, because resource does not exist!");
            return;
        }

        System.out.println(">>> [APP @ RagService: INGESTION] Step 1/4 - DONE!");

        // Step 2: Read the resource
        System.out.println(">>> [APP @ RagService: INGESTION] Step 2/4 - Reading document ...");
        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(resource);
        List<Document> pages = pdfReader.read();
        System.out.println(">>> [APP @ RagService: INGESTION] Step 2/4 - DONE! Read " + pages.size() + " pages.");

        // Step 3: Split the pages into smaller chunks
        System.out.println(">>> [APP @ RagService: INGESTION] Step 3/4 - Splitting pages into smaller chunks ...");
        TokenTextSplitter textSplitter = TokenTextSplitter.builder().build(); // Use chunk size, etc. here (optional)
        List<Document> chunks = textSplitter.apply(pages);
        System.out.println(">>> [APP @ RagService: INGESTION] Step 3/4 - DONE! Split into " + chunks.size() + " chunks.");

        // Step 4: Add chunks to with embeddings to the vector store
        System.out.println(">>> [APP @ RagService: INGESTION] Step 4/4 - Create embeddings and save them to vector store ...");
        vectorStore.add(chunks);
        System.out.println(">>> [APP @ RagService: INGESTION] Step 4/4 - DONE! Document saved into Vector Store.");
        System.out.println("=".repeat(60));
    }

    public String describe() {
        System.out.println("=".repeat(60));
        System.out.println(">>> [APP @ RagService: DESCRIBE] Starting RAG process ...");

        // Step 1: Search for similarities in the vector store
        System.out.println(">>> [APP @ RagService: DESCRIBE] Step 1/3 - Looking for similarities in the vector store ...");
        List<Document> relevantDocuments = vectorStore.similaritySearch(
                SearchRequest.builder().query(DESCRIBE_QUERY)
                        .topK(5)
                        .build()
        );
        System.out.println(">>> [APP @ RagService: DESCRIBE] Step 1/3 - DONE! Found " + relevantDocuments.size() + " relevant information.");

        if (relevantDocuments.isEmpty()) {
            System.err.println(">>> [APP @ RagService: DESCRIBE] Step 1/3 - Failed, because no relevant information could be found.");
            return "No relevant information found. Please make sure that the resource is available and ingested.";
        }

        // Step 2: Build context from chunks
        System.out.println(">>> [APP @ RagService: DESCRIBE] Step 2/3 - Building context from chunks ...");
        String context = relevantDocuments.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));
        System.out.println(">>> [APP @ RagService: DESCRIBE] Step 2/3 - DONE! Context built. Context size: " + context.length() + " characters.");

        // Step 3: Call LLM with context
        System.out.println(">>> [APP @ RagService: DESCRIBE] Step 3/3 - Building context from chunks ...");
        String response = chatClient.prompt()
                .system("""
                        You are a helpful assistant that can answer questions about a document.
                        Answer questions only on the basis of the provided context.
                        Answers in english and structured as a markdown list.
                        """)
                .user("Context from document: \n\n" + context + "\n\n---\n\nQuestion: " + DESCRIBE_QUERY)
                .call()
                .content();

        System.out.println(">>> [APP @ RagService: DESCRIBE] Answer get. Length: " + response.length() + " characters.");
        System.out.println("=".repeat(60));
        return response;
    }
}
