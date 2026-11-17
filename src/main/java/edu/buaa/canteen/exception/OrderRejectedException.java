package edu.buaa.canteen.exception;

public class OrderRejectedException extends RuntimeException {
    public OrderRejectedException(String message) { super(message); }
}
