package com.fisk.chartvisual;

import com.alibaba.fastjson.JSON;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class CodeTest {

    @Test
    public void Test() {
        List<Order> order = buildOrder(3);

        double sum = order.stream().flatMap(e -> e.orderItems.stream())
                .mapToDouble(e -> e.price * e.size)
                .sum();
        System.out.println(sum);

        System.out.println(JSON.toJSONString(order));
        order.forEach(e -> {
            e.orderName = "";
        });
        System.out.println(JSON.toJSONString(order));

        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
        list.forEach(e -> {
            if (e == 3) {
                e = 0;
            }
            System.out.println(e);
        });
        System.out.println(list);
    }

    public class Order {
        public String orderName;
        public String orderNumber;
        public List<OrderItem> orderItems;
    }

    public class OrderItem {
        public int projectId;
        public int size;
        public double price;
    }

    public List<Order> buildOrder(int num) {
        List<Order> list = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            Order order = new Order();
            order.orderName = "test";
            order.orderItems = buildOrderItem(new Random().nextInt(5));
            list.add(order);
        }
        return list;
    }

    public List<OrderItem> buildOrderItem(int num) {
        List<OrderItem> list = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            OrderItem item = new OrderItem();
            item.size = new Random().nextInt(10);
            item.price = 100.00;
            list.add(item);
        }
        return list;
    }
}
