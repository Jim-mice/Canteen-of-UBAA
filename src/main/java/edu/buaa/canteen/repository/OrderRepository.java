package edu.buaa.canteen.repository;

import edu.buaa.canteen.domain.order.Order;
import edu.buaa.canteen.domain.order.OrderStatus;
import java.util.List;
import java.util.Optional;

public interface OrderRepository { Optional<Order> findById(String id); Order save(Order order); List<Order> findByStatus(OrderStatus status); }
