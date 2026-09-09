package lk.ijse.bookstore.config;

import lk.ijse.bookstore.entity.*;
import lk.ijse.bookstore.enumiration.RoleType;
import lk.ijse.bookstore.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final WishlistRepository wishlistRepository;
    private final PasswordEncoder passwordEncoder;

    private final CategoryRepository categoryRepository;
    private final AuthorRepository authorRepository;
    private final PublisherRepository publisherRepository;
    private final BookRepository bookRepository;
    private final InventoryRepository inventoryRepository;

    @Override
    public void run(String... args) {
        seedRoles();
        seedAdmin();
        seedCatalog();
    }

    private void seedRoles() {
        for (RoleType type : RoleType.values()) {
            roleRepository.findByRoleName(type).orElseGet(() -> {
                log.info("Seeding role {}", type);
                return roleRepository.save(Role.builder().roleName(type).build());
            });
        }
    }

    private void seedAdmin() {
        String adminEmail = "admin@book.lk";
        if (userRepository.existsByEmail(adminEmail)) {
            return;
        }

        Role adminRole = roleRepository.findByRoleName(RoleType.ADMIN).orElseThrow();
        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);

        User admin = User.builder()
                .fullName("Admin User")
                .email(adminEmail)
                .password(passwordEncoder.encode("admin123"))
                .phone("0770000000")
                .enabled(true)
                .roles(roles)
                .build();
        admin = userRepository.save(admin);

        cartRepository.save(Cart.builder().user(admin).build());
        wishlistRepository.save(Wishlist.builder().user(admin).build());

        log.info("Seeded default admin account: {} / Admin@123 (CHANGE THIS before submission)", adminEmail);
    }

    private void seedCatalog() {
        if (bookRepository.count() > 0) {
            return;
        }

        Category fiction = categoryRepository.save(Category.builder().name("Fiction").description("Novels & short stories").build());
        Category education = categoryRepository.save(Category.builder().name("Education").description("School textbooks").build());
        Category poetry = categoryRepository.save(Category.builder().name("Poetry").description("Poetry collections").build());

        Author author1 = authorRepository.save(Author.builder().name("K. James").bio("Contemporary Sri Lankan novelist").build());
        Author author2 = authorRepository.save(Author.builder().name("Dept. of Education").bio("Official school curriculum").build());

        Publisher publisher = publisherRepository.save(Publisher.builder().name("Godage International").contactEmail("info@godage.lk").build());

        Book book1 = Book.builder()
                .title("The Silence of the Sea")
                .isbn("978-955-0000-01-1")
                .description("A quiet coastal town, a family secret decades old, and the tide that finally brings it in.")
                .price(new BigDecimal("1450.00"))
                .specialPrice(new BigDecimal("1190.00"))
                .language("English")
                .pages(300)
                .publisher(publisher)
                .categories(Set.of(fiction))
                .authors(Set.of(author1))
                .build();
        book1 = bookRepository.save(book1);
        inventoryRepository.save(Inventory.builder().book(book1).quantityAvailable(42).reorderLevel(10).build());

        Book book2 = Book.builder()
                .title("Mathematics for Grade 11")
                .isbn("978-955-0000-02-2")
                .description("Official Department of Education syllabus textbook.")
                .price(new BigDecimal("620.00"))
                .language("Sinhala")
                .pages(200)
                .publisher(publisher)
                .categories(Set.of(education))
                .authors(Set.of(author2))
                .build();
        book2 = bookRepository.save(book2);
        inventoryRepository.save(Inventory.builder().book(book2).quantityAvailable(58).reorderLevel(15).build());

        Book book3 = Book.builder()
                .title("Whispers of the Highlands")
                .isbn("978-955-0000-03-3")
                .description("A poetry collection inspired by Sri Lanka's central hills.")
                .price(new BigDecimal("890.00"))
                .specialPrice(new BigDecimal("690.00"))
                .language("English")
                .pages(90)
                .publisher(publisher)
                .categories(Set.of(poetry))
                .authors(Set.of(author1))
                .build();
        book3 = bookRepository.save(book3);
        inventoryRepository.save(Inventory.builder().book(book3).quantityAvailable(14).reorderLevel(5).build());

        log.info("Seeded demo catalog: 3 categories, 2 authors, 1 publisher, 3 books.");
    }
}
