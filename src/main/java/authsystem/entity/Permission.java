package authsystem.entity;
import lombok.Data;
import javax.persistence.*;
import java.util.Set;

@Entity
@Data
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;
}
