package org.example.config.model;

/**
 * Output options configuration section.
 */
public class OutputOptions {
    private boolean lombok;

    public boolean isLombok() {
        return lombok;
    }

    public void setLombok(boolean lombok) {
        this.lombok = lombok;
    }

    @Override
    public String toString() {
        return "OutputOptions{" +
            "lombok=" + lombok +
            '}';
    }
}