package org.example.cli.command.impl;

import org.example.cli.command.Command;
import org.example.generator.docs.DocGenerator;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

public class InitCommand implements Command {

    private final DocGenerator docGenerator = new DocGenerator();
    private static final String DEFAULT_CONFIG_PATH = "arch.yml";

    @Override
    public void execute(String[] args) {
        try {
            String configPath = args.length > 0 ? args[0] : DEFAULT_CONFIG_PATH;
            System.out.println("Initializing project configuration at: " + configPath);

            // Verificar si el archivo ya existe
            if (Files.exists(Paths.get(configPath))) {
                System.out.println("⚠️  Configuration file already exists at: " + configPath);
                return;
            }

            // Generar archivos y esperar a que termine
            CompletableFuture<Void> generationFuture = CompletableFuture.runAsync(() -> {
                docGenerator.run();
            });

            generationFuture.join(); // Espera a que la generación termine

            // Verificar que los archivos se crearon correctamente
            if (Files.exists(Paths.get("arch.yml")) && Files.exists(Paths.get("arch.md"))) {
                System.out.println("✅ Files created successfully");
            } else {
                System.err.println("❌ Failed to create one or more files");
            }

        } catch (Exception e) {
            System.err.println("❌ Initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String getDescription() {
        return "generate the initials file for the app use";
    }
}
