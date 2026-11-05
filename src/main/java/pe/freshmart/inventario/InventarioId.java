
package pe.freshmart.inventario;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class InventarioId implements Serializable {

    @Column(name = "id_producto")
    private Integer idProducto;

    @Column(name = "id_tienda")
    private Integer idTienda;

    public InventarioId() {
    }

    public InventarioId(Integer idProducto, Integer idTienda) {
        this.idProducto = idProducto;
        this.idTienda = idTienda;
    }

    public Integer getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }

    public Integer getIdTienda() {
        return idTienda;
    }

    public void setIdTienda(Integer idTienda) {
        this.idTienda = idTienda;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InventarioId)) return false;

        InventarioId that = (InventarioId) o;

        return Objects.equals(idProducto, that.idProducto)
            && Objects.equals(idTienda, that.idTienda);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idProducto, idTienda);
    }
}
