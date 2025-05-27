package org.example.generator.docs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DocGenerator {

    public void run() {
        generateYamlFile();
        generateReadmeFile();
        System.out.println("✅ arch.yaml and arch.md files generated successfully");
    }

    private void generateYamlFile() {
        String yamlContent = """
            version: "1.0"
            application:
              build: "gradle"
            
            sql:
              engine: "postgresql"
              schema:
                path:
                  - "file1.sql"
                  - "file2.sql"
            
                directory: "directory"
            
            output:
              basePackage: "com.example.demo"
              options:
                lombok: false
            """;

        writeToFile("arch.yml", yamlContent);
    }

    private void generateReadmeFile() {
        String readmeContent = """
            # Archetype Generator
            
            Archetype is a tool that generates Java entity classes from an Postgres SQL schema.
            
            ## Configuration File Structure (arch.yml)
            
            After running `arc init`, a `arch.yml` file will be created that needs to be configured. Here is an example of how it should look:
            
            ```yaml
            version: "1.0"
            application:
              build: "gradle" # Options: gradle, maven to define the initial folder structure
            
            sql:
              engine: "postgresql"  # Options: postgresql, mysql, oracle, sqlserver to subsequently define configurations in application.properties or application.yml
              schema:
                path:
                  - "file1.sql"
                  - "file2.sql"
            
                directory: "directory" # Directory where the SQL files are located
            
            output:
              basePackage: "com.example.demo" # Separate by periods or slashes, consider Windows option as well
              options:
                lombok: false # true/false to enable/disable Lombok
            ```
            
            ## SQL Schema Structure
            
            For Archetype to work properly, the SQL schema must have the relationships between tables defined within the `CREATE TABLE` statements, or use `ALTER TABLE` commands.
            
            Here is an example of a compatible SQL schema:
            
            ```sql
            CREATE TABLE category (
                id SERIAL PRIMARY KEY,
                name VARCHAR(100) NOT NULL
            );
            
            CREATE TABLE product (
                id SERIAL PRIMARY KEY,
                name VARCHAR(100) NOT NULL DEFAULT 'Unnamed Product',
                price DECIMAL(10,2) NOT NULL,
                stock INTEGER NOT NULL,
                category_id INTEGER REFERENCES category(id)
            );
            
            ALTER TABLE category
            ADD COLUMN description TEXT;
            
            ALTER TABLE category
            ALTER COLUMN name TYPE VARCHAR(200);
            
            ALTER TABLE category
            ADD CONSTRAINT uq_category_name UNIQUE (name);
            
            ALTER TABLE product
            ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
            
            ALTER TABLE product
            ADD CONSTRAINT chk_product_stock_positive CHECK (stock >= 0);
            
            ALTER TABLE product
            RENAME COLUMN price TO unit_price;
            
            ALTER TABLE product
            ADD CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE CASCADE;
            ```
            
            With this configuration and the correct schema, you can run `arc generate models or arc g m` to generate the corresponding Java entity classes.
            
            Ready to get started!
            """;

        // Eliminar el espaciado hacia la izquierda en cada línea
        String formattedContent = readmeContent.stripIndent();

        writeToFile("arch.md", formattedContent);
    }

    private void writeToFile(String fileName, String content) {
        try {
            Files.write(Paths.get(fileName), content.getBytes());
        } catch (IOException e) {
            System.err.println("❌ Error writing " + fileName + ": " + e.getMessage());
        }
    }
}
