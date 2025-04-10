package org.example.cli.command;

public interface Command {
    void execute(String[] args);

    String getDescription();
}