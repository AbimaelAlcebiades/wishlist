package abimael.wishlist.infrastructure.services;

import abimael.wishlist.domain.models.ClientWishList;
import abimael.wishlist.infrastructure.services.exceptions.*;

import java.util.List;
import java.util.Optional;

public interface WishListService {

  // List<ClientWishList> getAllWishLists() throws ServiceException;

  ClientWishList addProductToWishList(Long clientId, Long productId)
      throws ServiceException, MaxProductsInClientWishListException,
          ProductAlreadyExistsInWishListException;

  Optional<ClientWishList> getClientWishListByClientId(Long clientId) throws ServiceException;

  void removeProductFromWishList(Long clientId, Long productId)
      throws ServiceException, ClientNotFoundException, ProductNotFoundInWishListException;

  List<Long> getWishListProductsByClientId(Long clientId)
      throws ServiceException, ClientNotFoundException;

  Long getWishListProductByClientId(Long clientId, Long productId)
      throws ServiceException, ClientNotFoundException, ProductNotFoundInWishListException;

  /*Optional<Product> getProductById(String id);

  Product addProduct(CreateProductDto createProductDto);

  void removeProduct(String id);

  List<Product> getWishListClient(UUID clientId);*/
}
