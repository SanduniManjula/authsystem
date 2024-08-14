package authsystem.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
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
            //joinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"),
           // inverseJoinColumns = @JoinColumn(name = "permission_id", referencedColumnName = "id")

    )
    private Set<Permission> permissions = new HashSet<>();



}
