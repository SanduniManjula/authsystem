package authsystem.entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "dual_auth_system")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DualAuthSystem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String entity;

    @Column(columnDefinition = "TEXT")
    private String oldData;

    @Column(columnDefinition = "TEXT")
    private String newData;

    private Long createdBy;

    private Long reviewedBy;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private Action action;

    // private LocalDateTime createdAt;

    // private LocalDateTime updatedAt;

    public enum Status {
        PENDING,
        APPROVED,
        REJECTED
    }
    public enum Action {
        CREATE,
        UPDATE,
        DELETE
    }
}
