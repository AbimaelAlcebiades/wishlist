package abimael.wishlist.infrastructure.services;

import abimael.wishlist.domain.models.ClientWishList;
import abimael.wishlist.domain.models.WishList;
import abimael.wishlist.domain.repositories.WishListRepository;
import abimael.wishlist.infrastructure.configs.WishListApplicationConfig;
import abimael.wishlist.infrastructure.services.exceptions.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class WishListServiceImpl implements WishListService {

  private final WishListRepository wishListRepository;
  private final WishListApplicationConfig wishListApplicationConfig;

  /*@Override
  @Cacheable(value = "wishlists")
  public List<ClientWishList> getAllWishLists() throws ServiceException {
    try {
      return wishListRepository.findAll();
    } catch (Exception e) {
      throw new ServiceException("Erro on find all wish lists. " + e.getMessage());
    }
  }*/

  @Override
  @CacheEvict(value = "wishlists", allEntries = true, key = "'clientId:' + #clientId")
  public ClientWishList addProductToWishList(Long clientId, Long productId)
      throws ServiceException, MaxProductsInClientWishListException,
          ProductAlreadyExistsInWishListException {

    try {
      ClientWishList clientWishToSave = buildClientWishListFrom(clientId, productId);

      return wishListRepository.save(clientWishToSave);
    } catch (ProductAlreadyExistsInWishListException | MaxProductsInClientWishListException e) {
      throw e;
    } catch (Exception e) {
      throw new ServiceException(
          "Erro on add product "
              + productId
              + " for client id "
              + clientId
              + ". "
              + e.getMessage());
    }
  }

  public ClientWishList buildClientWishListFrom(Long clientId, Long productId)
      throws ServiceException, MaxProductsInClientWishListException,
          ProductAlreadyExistsInWishListException, JsonProcessingException {
    Optional<ClientWishList> clientWishList = this.getClientWishListByClientId(clientId);

    boolean isNewWishList = clientWishList.isEmpty();

    ClientWishList clientWishToSave;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    if (isNewWishList) {
      clientWishToSave = buildNewClientWishList(clientId, productId);
      clientWishToSave.setCreatedAt(ZonedDateTime.now().format(formatter));
    } else {

      if (!isWishListWithLimitToAddProduct(clientWishList.get())) {
        throw new MaxProductsInClientWishListException(
            "Maximum number of products reached in client's wish list. The actual max value is "
                + wishListApplicationConfig.getMaxItemsInWishList()
                + " and client wish list have "
                + clientWishList.get().getWishList().getProductsIds().size());
      }

      if (isProductExistsWishList(clientWishList.get(), productId)) {
        throw new ProductAlreadyExistsInWishListException(
            "Product with id " + productId + " already exists in wish list");
      }

      clientWishToSave = buildWishListAddingProductId(clientWishList.get(), productId);

      // productsIds.add(productId);
      // clientWishToSave.getWishList().setProductsIds(productsIds);

      clientWishToSave.setUpdatedAt(ZonedDateTime.now().format(formatter));
    }
    return clientWishToSave;
  }

  private ClientWishList buildWishListAddingProductId(ClientWishList clientWishList, Long productId)
      throws JsonProcessingException {

    ObjectMapper mapper = new ObjectMapper();

    ClientWishList clientWishListCopy =
        mapper.readValue(mapper.writeValueAsString(clientWishList), ClientWishList.class);

    clientWishListCopy.getWishList().getProductsIds().add(productId);
    clientWishListCopy
        .getWishList()
        .setTotalProductionInList(clientWishListCopy.getWishList().getProductsIds().size());
    return clientWishListCopy;
  }

  private ClientWishList buildWishListRemovingProductId(
      ClientWishList clientWishList, Long productId) throws JsonProcessingException {

    ObjectMapper mapper = new ObjectMapper();

    ClientWishList clientWishListCopy =
        mapper.readValue(mapper.writeValueAsString(clientWishList), ClientWishList.class);

    clientWishListCopy.getWishList().getProductsIds().remove(productId);
    clientWishListCopy.getWishList().getProductsIds().removeIf(n -> n.equals(productId));

    clientWishListCopy
        .getWishList()
        .setTotalProductionInList(clientWishListCopy.getWishList().getProductsIds().size());
    return clientWishListCopy;
  }

  private boolean isProductExistsWishList(ClientWishList clientWishList, Long productId) {
    return clientWishList.getWishList().getProductsIds().contains(productId);
  }

  private boolean isWishListWithLimitToAddProduct(ClientWishList clientWishList) {
    List<Long> productsIds = clientWishList.getWishList().getProductsIds();

    return productsIds.size() < wishListApplicationConfig.getMaxItemsInWishList();
  }

  private ClientWishList buildNewClientWishList(Long clientId, Long productId) {
    return ClientWishList.builder()
        .clientId(clientId)
        .wishList(
            WishList.builder()
                .productsIds(Collections.singletonList(productId))
                .totalProductionInList(1)
                .build())
        .build();
  }

  @Override
  @Cacheable(value = "wishlists", key = "'clientId:' + #clientId")
  public Optional<ClientWishList> getClientWishListByClientId(Long clientId)
      throws ServiceException {
    try {
      return wishListRepository.findByClientId(clientId);
    } catch (Exception e) {
      throw new ServiceException("Erro on get clientId " + clientId + "." + e.getMessage());
    }
  }

  @Override
  public void removeProductFromWishList(Long clientId, Long productId)
      throws ServiceException, ClientNotFoundException, ProductNotFoundInWishListException {

    try {
      ClientWishList clientWishList = getClientWishListOrThrowException(clientId);

      getProductIdOrThrowException(clientWishList, productId);

      wishListRepository.save(buildWishListRemovingProductId(clientWishList, productId));
    } catch (ClientNotFoundException | ProductNotFoundInWishListException e) {
      throw e;
    } catch (Exception e) {
      throw new ServiceException(
          "Erro on remove product id "
              + productId
              + " from wishlist client id ."
              + clientId
              + e.getMessage());
    }
  }

  private ClientWishList getClientWishListOrThrowException(Long clientId)
      throws ServiceException, ClientNotFoundException {
    return getClientWishListByClientId(clientId)
        .orElseThrow(() -> new ClientNotFoundException("Client id " + clientId + " not found"));
  }

  private Long getProductIdOrThrowException(ClientWishList clientWishList, Long productId)
      throws ProductNotFoundInWishListException {

    if (!isProductExistsWishList(clientWishList, productId)) {
      throw new ProductNotFoundInWishListException("Product id " + productId + " does not exits");
    }

    return productId;
  }

  @Override
  public List<Long> getWishListProductsByClientId(Long clientId)
      throws ServiceException, ClientNotFoundException {
    try {
      ClientWishList clientWishList = getClientWishListOrThrowException(clientId);

      return clientWishList.getWishList().getProductsIds();

    } catch (ClientNotFoundException e) {
      throw e;
    } catch (Exception e) {
      throw new ServiceException(
          "Error on get wish list products by client id " + clientId + ". " + e.getMessage());
    }
  }

  @Override
  public Long getWishListProductByClientId(Long clientId, Long productId)
      throws ServiceException, ClientNotFoundException, ProductNotFoundInWishListException {

    try {
      ClientWishList clientWishList = getClientWishListOrThrowException(clientId);

      return getProductIdOrThrowException(clientWishList, productId);
    } catch (ClientNotFoundException | ProductNotFoundInWishListException e) {
      throw e;
    } catch (Exception e) {
      throw new ServiceException(
          "Error on get wish list product by client id " + clientId + ". " + e.getMessage());
    }
  }

  /*public Optional<Product> getProductById(String id) {
    List<Product> allProducts = getAllWishLists();
    return allProducts.stream().filter(item -> item.getId().equals(id)).findAny();
  }

  @CacheEvict(value = "wishlist", allEntries = true)
  public Product addProduct(CreateProductDto createProductDto) {
    return wishListRepository.save(buildProductFrom(createProductDto));
  }

  private Product buildProductFrom(CreateProductDto createProductDto) {
    return Product.builder()
        .name(createProductDto.getName())
        .description(createProductDto.getDescription())
        .price(createProductDto.getPrice())
        .build();
  }

  @CacheEvict(value = "wishlist", allEntries = true)
  public void removeProduct(String id) {
    wishListRepository.deleteById(id);
  }

  @Override
  public List<Product> getWishListClient(UUID clientId) {
    return null;
  }*/
}
