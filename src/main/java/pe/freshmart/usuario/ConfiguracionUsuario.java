package pe.freshmart.usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "configuracion_usuario")
public class ConfiguracionUsuario {
    @Id
    @Column(name = "id_usuario")
    private Integer idUsuario;
    @Column(name = "notif_pedidos", nullable = false)
    private boolean notifPedidos = true;
    @Column(name = "notif_promociones", nullable = false)
    private boolean notifPromociones = true;
    @Column(name = "notif_correo", nullable = false)
    private boolean notifCorreo;
    @Column(nullable = false, length = 10)
    private String tema = "CLARO";

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }
    public boolean isNotifPedidos() { return notifPedidos; }
    public void setNotifPedidos(boolean notifPedidos) { this.notifPedidos = notifPedidos; }
    public boolean isNotifPromociones() { return notifPromociones; }
    public void setNotifPromociones(boolean notifPromociones) { this.notifPromociones = notifPromociones; }
    public boolean isNotifCorreo() { return notifCorreo; }
    public void setNotifCorreo(boolean notifCorreo) { this.notifCorreo = notifCorreo; }
    public String getTema() { return tema; }
    public void setTema(String tema) { this.tema = tema; }
}
