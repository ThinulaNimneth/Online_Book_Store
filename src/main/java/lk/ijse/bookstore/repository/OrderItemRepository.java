package lk.ijse.bookstore.repository;

import lk.ijse.bookstore.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    @Query("select  oi from  OrderItem oi group by oi.book order by  sum(oi.quantity) desc ")
    List<OrderItem> findBestsellingBooksAllTime();
}
