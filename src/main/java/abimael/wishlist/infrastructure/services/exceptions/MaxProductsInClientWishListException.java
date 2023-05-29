package abimael.wishlist.infrastructure.services.exceptions;

public class MaxProductsInClientWishListException extends Exception {

  public MaxProductsInClientWishListException(String message) {
    super(message);
  }
}
