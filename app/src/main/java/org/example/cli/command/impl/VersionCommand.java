package org.example.cli.command.impl;

import org.example.cli.command.Command;

public class VersionCommand implements Command {

    private static final String VERSION = "1.0.0";

    @Override
    public void execute(String[] args) {
        System.out.println("Archetype CLI version " + VERSION);
    }

    @Override
    public String getDescription() {
        return "Show version information";
    }
}