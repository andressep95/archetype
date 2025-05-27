package org.example.cli.command;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.example.cli.command.impl.CheckCommand;
import org.example.cli.command.impl.GenerateCommand;
import org.example.cli.command.impl.InitCommand;
import org.example.cli.command.impl.VersionCommand;

public class CommandRegistry {
    private static CommandRegistry instance;
    private final Map<String, Command> commands;
    private final ExecutorService executor;

    private CommandRegistry() {
        commands = new HashMap<>();
        executor = Executors.newCachedThreadPool();
    }

    public static synchronized CommandRegistry getInstance() {
        if (instance == null) {
            instance = new CommandRegistry();
        }
        return instance;
    }

    public void registerCommand(String name, String shorthand, Command command) {
        commands.put(name.toLowerCase(), command);
        commands.put(shorthand.toLowerCase(), command);
    }

    public Command getCommand(String name) {
        return commands.get(name.toLowerCase());
    }

    public boolean hasCommand(String name) {
        return commands.containsKey(name.toLowerCase());
    }

    public void registerDefaultCommands() {
        registerCommand("init", "i", new InitCommand());
        registerCommand("check", "c", new CheckCommand());
        registerCommand("generate", "g", new GenerateCommand());
        registerCommand("version", "v", new VersionCommand());
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public void shutdown() {
        executor.shutdown();
    }
}