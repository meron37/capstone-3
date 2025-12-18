package org.yearup.models;

import java.math.BigDecimal;

public class OrderLineItem
{
    private int orderId;
    private int productId;
    private BigDecimal price = BigDecimal.ZERO;
    private int quantity;

    public int getOrderId() {
        return orderId; }
    public void setOrderId(int orderId) {
        this.orderId = orderId; }

    public int getProductId() {
        return productId; }
    public void setProductId(int productId)
    { this.productId = productId; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price)
    { this.price = price; }

    public int getQuantity()
    { return quantity; }
    public void setQuantity(int quantity)
    { this.quantity = quantity; }
}
