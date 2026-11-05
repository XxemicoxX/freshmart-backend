
package pe.freshmart.inventario;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "inventario")
public class Inventario {

    @EmbeddedId
    private InventarioId id;

    @Column(nullable = false)
    private Integer stock;

    public Inventario() {
    }

    public Inventario(InventarioId id, Integer stock) {
        this.id = id;
        this.stock = stock;
    }

    public InventarioId getId() {
        return id;
    }

    public void setId(InventarioId id) {
        this.id = id;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }
}
