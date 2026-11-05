package pe.freshmart.tienda;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tienda")
public class Tienda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tienda")
    private Integer id;
    @Column(nullable = false, length = 100)
    private String nombre;
    @Column(nullable = false)
    private String direccion;
    private String telefono;
    @Column(name = "horario_atencion")
    private String horarioAtencion;
    @Column(nullable = false)
    private boolean activa = true;

    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDireccion() { return direccion; }
    public String getTelefono() { return telefono; }
    public String getHorarioAtencion() { return horarioAtencion; }
    public boolean isActiva() { return activa; }
}
