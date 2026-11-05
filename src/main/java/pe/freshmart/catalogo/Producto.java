package pe.freshmart.catalogo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "producto")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;
    @Column(nullable = false, length = 150)
    private String nombre;
    private String descripcion;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;
    @Column(name = "calificacion_promedio", precision = 2, scale = 1)
    private BigDecimal calificacion;
    @Column(name = "imagen_url")
    private String imagenUrl;
    @Column(nullable = false)
    private boolean activo = true;

    public Integer getId() { return id; }
    public Categoria getCategoria() { return categoria; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public BigDecimal getPrecio() { return precio; }
    public BigDecimal getCalificacion() { return calificacion; }
    public String getImagenUrl() { return imagenUrl; }
    public boolean isActivo() { return activo; }
}
