package org.example.command;

import java.util.concurrent.CompletableFuture;

public class CommandExecutor {

    public static void executeCommand(String commandName, String[] args) {
        CommandRegistry registry = CommandRegistry.getInstance();

        if (registry.hasCommand(commandName)) {
            Command command = registry.getCommand(commandName);

            // Execute command asynchronously
            CompletableFuture.runAsync(() -> {
                try {
                    command.execute(args);
                } catch (Exception e) {
                    System.err.println("❌ Error executing command '" + commandName + "': " + e.getMessage());
                }
            }, registry.getExecutor()).join(); // Wait for completion

        } else {
            System.err.println("❌ Unknown command: " + commandName);
            System.err.println("Run 'arch help' for a list of available commands");
        }
    }
}