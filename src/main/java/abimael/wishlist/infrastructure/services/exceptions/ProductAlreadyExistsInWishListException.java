package abimael.wishlist.infrastructure.services.exceptions;

public class ProductAlreadyExistsInWishListException extends Exception {

  public ProductAlreadyExistsInWishListException(String message) {
    super(message);
  }
}
