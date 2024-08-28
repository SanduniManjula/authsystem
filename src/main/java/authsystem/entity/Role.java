package authsystem.entity;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Data
@NoArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")


    )
    private Set<Permission> permissions = new HashSet<>();

    @Column(nullable = false)
    private boolean activated;

    @Column(nullable = false)
    private boolean locked = true;


    public Role(String name, Set<Permission> permissions, boolean activated) {
        this.name = name;
        this.permissions = permissions;
        this.activated = activated;
    }


}
