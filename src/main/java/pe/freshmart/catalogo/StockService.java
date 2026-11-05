package pe.freshmart.catalogo;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class StockService {
    private final JdbcTemplate jdbc;

    public StockService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public int stock(Integer productoId, Integer tiendaId) {
        return stocks(java.util.List.of(productoId), tiendaId).getOrDefault(productoId, 0);
    }

    public Map<Integer, Integer> stocks(Collection<Integer> productoIds, Integer tiendaId) {
        if (productoIds == null || productoIds.isEmpty()) return Map.of();
        String placeholders = String.join(",", java.util.Collections.nCopies(productoIds.size(), "?"));
        String sql = "select id_producto, sum(stock) from inventario where id_producto in (" + placeholders + ")";
        Object[] args;
        if (tiendaId == null) {
            sql += " group by id_producto";
            args = productoIds.toArray();
        } else {
            sql += " and id_tienda = ? group by id_producto";
            args = new Object[productoIds.size() + 1];
            int index = 0;
            for (Integer id : productoIds) args[index++] = id;
            args[index] = tiendaId;
        }
        Map<Integer, Integer> resultado = new HashMap<>();
        jdbc.query(sql, rs -> { resultado.put(rs.getInt(1), rs.getInt(2)); }, args);
        return resultado;
    }
}
