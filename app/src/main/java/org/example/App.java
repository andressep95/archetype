package org.example;

import org.example.command.CommandExecutor;
import org.example.command.CommandRegistry;
import org.example.exception.ConfigurationException;

import java.util.Arrays;

public class App {

    public static void main(String[] args) {

        // Register all available commands
        CommandRegistry registry = CommandRegistry.getInstance();
        registry.registerDefaultCommands();

        if (args.length == 0 || "--help".equals(args[0]) || "-h".equals(args[0])) {
            printHelp();
            return;
        }

        try {
            // Extract command and remaining arguments
            String commandName = args[0];
            String[] commandArgs = Arrays.copyOfRange(args, 1, args.length);

            // Execute the command asynchronously
            CommandExecutor.executeCommand(commandName, commandArgs);

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            if (!(e instanceof ConfigurationException)) {
                System.err.println("❌ An unexpected error occurred. Please report this issue.");
            }
        } finally {
            // Ensure clean shutdown
            CommandRegistry.getInstance().shutdown();
        }
    }

    private static void printHelp() {
        System.out.println("""
            Usage:
              arch [command] [options]
            
            Commands:
              config, c [path]     Read configuration from path for debug(default: arch.yml)
              init, i              Generate configuration file with basic settings
              generate, g [type]   Generate code from SQL schema
                Types:
                  models, m        Generate model classes
                  repos, r         Generate repository classes
                  services, s      Generate service classes
                  controllers, c.  Generate controller classes
                  all, a           Generate all code artifacts
              process, p           Process SQL schema with current configuration
              version, v           Show the tool's version
              help, h              Display this help message
            
            Examples:
              arch generate models   Generate model classes (full command)
              arch g m               Generate model classes (shorthand)
              arch c custom.yml      Use custom configuration file
            """
        );
    }
}