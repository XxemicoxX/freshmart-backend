package pe.freshmart.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "token_revocado")
public class TokenRevocado {
    @Id
    @Column(nullable = false, length = 100)
    private String jti;
    @Column(name = "id_usuario", nullable = false)
    private Integer usuarioId;
    @Column(name = "vence_en", nullable = false)
    private LocalDateTime venceEn;
}
