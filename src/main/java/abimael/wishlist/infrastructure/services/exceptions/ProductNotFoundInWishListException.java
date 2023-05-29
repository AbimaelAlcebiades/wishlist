package abimael.wishlist.infrastructure.services.exceptions;

public class ProductNotFoundInWishListException extends Exception {

  public ProductNotFoundInWishListException(String message) {
    super(message);
  }
}
