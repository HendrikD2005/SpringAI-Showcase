package com.hendrikd.springaishowcase.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;

import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * Component class for handling with resources.
 */
@Component
public class ResourceComponent implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceComponent.class);

    private final VectorStore vectorStore;

    @Value("classpath:TIOBE.pdf")
    private Resource marketPDF;

    /**
     * Constructor
     * <p>
     *     Initialization of the vector store.
     * </p>
     *
     * @param vectorStore Vector store for the application
     */
    public ResourceComponent(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /**
     * Run Method
     * <p>
     *     Uses the {@link PagePdfDocumentReader} to read from the classpath resource file. The data from the resource
     *     file gets splitted into smaller chunks by the {@link TokenTextSplitter} and gets stored as vectors in the
     *     vector store.
     * </p>
     * @param args
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {
        LOGGER.info("Loading vectors from file {}", marketPDF);
        var pdfReader = new PagePdfDocumentReader(marketPDF);
        TextSplitter textSplitter = new TokenTextSplitter();

        vectorStore.accept(textSplitter.apply(pdfReader.get()));
        vectorStore.add(textSplitter.apply(pdfReader.get()));

        LOGGER.info("Vector Store loaded with data!");
    }

}
