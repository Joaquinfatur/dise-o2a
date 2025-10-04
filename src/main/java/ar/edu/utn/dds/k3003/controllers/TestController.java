package ar.edu.utn.dds.k3003.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
public class TestController {
    
    @Autowired
    private DataSource dataSource;
    
    @GetMapping("/create-tables")
    public ResponseEntity<String> createTables() {
        try {
            var connection = dataSource.getConnection();
            var statement = connection.createStatement();
            
            statement.execute("CREATE TABLE IF NOT EXISTS pdis (id SERIAL PRIMARY KEY, hecho_id VARCHAR(255), contenido TEXT, ubicacion VARCHAR(255), fecha TIMESTAMP, usuario_id VARCHAR(255), imagen_url VARCHAR(500), ocr_resultado TEXT, etiquetado_resultado TEXT, procesado BOOLEAN DEFAULT false)");
            
            statement.execute("CREATE TABLE IF NOT EXISTS pdi_etiquetas_deprecated (pdi_id INTEGER REFERENCES pdis(id) ON DELETE CASCADE, etiqueta VARCHAR(255))");
            
            statement.execute("CREATE TABLE IF NOT EXISTS pdi_etiquetas_nuevas (pdi_id INTEGER REFERENCES pdis(id) ON DELETE CASCADE, etiqueta VARCHAR(255))");
            
            connection.close();
            
            return ResponseEntity.ok("Tablas creadas!");
        } catch (Exception e) {
            return ResponseEntity.ok("Error: " + e.getMessage());
        }
    }
}