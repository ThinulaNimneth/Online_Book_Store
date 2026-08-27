package lk.ijse.bookstore.entity;

import jakarta.persistence.*;
import lk.ijse.bookstore.enumiration.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Data
@EqualsAndHashCode(of = "roleId")
@ToString(of = "roleId")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 20)
    private RoleType roleName;

    @ManyToMany(mappedBy = "roles")
    @Builder.Default
    private Set<User> users  =  new HashSet<>();
}
