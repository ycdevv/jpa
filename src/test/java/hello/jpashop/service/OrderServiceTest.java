package hello.jpashop.service;

import hello.jpashop.domain.Address;
import hello.jpashop.domain.Member;
import hello.jpashop.domain.Order;
import hello.jpashop.domain.OrderStatus;
import hello.jpashop.domain.item.Book;
import hello.jpashop.domain.item.Item;
import hello.jpashop.exception.NotEnoughStockException;
import hello.jpashop.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EntityManager em;

    @Test
    void orderTest() {
        //given
        Member member = createMember();
        Item item = createItem("JPA", 10000, 10);
        int orderCount = 2;

        //when
        Long orderId = orderService.order(member.getId(), item.getId(), orderCount);

        //then
        Order getOrder = orderRepository.findOne(orderId);
        assertThat(getOrder.getStatus()).isEqualTo(OrderStatus.ORDER);
        assertThat(getOrder.getOrderItems().size()).isEqualTo(1);
    }

    @Test
    void notEnoughStockExceptionTest() {
        //given
        Member member = createMember();
        Item item = createItem("JPA", 10000, 10);
        int orderCount = 11;

        //when, then
        assertThatThrownBy(() -> orderService.order(member.getId(), item.getId(), orderCount))
                .isInstanceOf(NotEnoughStockException.class);
    }

    @Test
    void orderCancelTest() {
        //given
        Member member = createMember();
        Item item = createItem("JPA", 10000, 10);
        int orderCount = 2;

        Long orderId = orderService.order(member.getId(), item.getId(), orderCount);

        //when
        orderService.cancelOrder(orderId);

        //then
        Order getOrder = orderRepository.findOne(orderId);
        assertThat(getOrder.getStatus()).isEqualTo(OrderStatus.CANCEL);
        assertThat(item.getStockQuantity()).isEqualTo(10);
    }

    private Item createItem(String name, int price, int quantity) {
        Item item = new Book();
        item.setName(name);
        item.setPrice(price);
        item.setStockQuantity(quantity);
        em.persist(item);
        return item;
    }

    private Member createMember() {
        Member member = new Member();
        member.setName("회원1");
        member.setAddress(new Address("서울", "서초", "123-123"));
        em.persist(member);
        return member;
    }
}
