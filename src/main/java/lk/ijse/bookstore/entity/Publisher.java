package lk.ijse.bookstore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "publishers")
@Data
@EqualsAndHashCode(of = "publisherId")
@ToString(of = "publisherId")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Publisher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long publisherId;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 120)
    private String contactEmail;

    @OneToMany(mappedBy = "publisher")
    @Builder.Default
    private Set<Book> books = new HashSet<>();
}