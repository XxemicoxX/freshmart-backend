package pe.freshmart.notificacion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "pregunta_frecuente")
public class PreguntaFrecuente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false, length = 60)
    private String categoria;
    @Column(nullable = false, length = 200)
    private String pregunta;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String respuesta;

    public Integer getId() { return id; }
    public String getCategoria() { return categoria; }
    public String getPregunta() { return pregunta; }
    public String getRespuesta() { return respuesta; }
}
