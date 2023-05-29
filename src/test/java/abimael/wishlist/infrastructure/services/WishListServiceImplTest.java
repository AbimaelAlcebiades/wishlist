package abimael.wishlist.infrastructure.services;

import abimael.wishlist.application.dtos.AddProductToWishListDto;
import abimael.wishlist.domain.models.ClientWishList;
import abimael.wishlist.domain.models.WishList;
import abimael.wishlist.domain.repositories.WishListRepository;
import abimael.wishlist.infrastructure.configs.WishListApplicationConfig;
import abimael.wishlist.infrastructure.services.exceptions.ClientNotFoundException;
import abimael.wishlist.infrastructure.services.exceptions.MaxProductsInClientWishListException;
import abimael.wishlist.infrastructure.services.exceptions.ProductAlreadyExistsInWishListException;
import abimael.wishlist.infrastructure.services.exceptions.ProductNotFoundInWishListException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishListServiceImplTest {

  @Mock private WishListRepository wishListRepository;

  @Mock private WishListApplicationConfig wishListApplicationConfig;

  @InjectMocks private WishListServiceImpl wishListServiceImpl;

  private void setupMaxItemsInWishList() {
    int maxItemInWishList = 20;
    when(wishListApplicationConfig.getMaxItemsInWishList()).thenReturn(maxItemInWishList);
  }

  /*@SneakyThrows
  @Test
  @DisplayName(
      "QUANDO o metodo getAllWishLists for chamado ENTAO deve ser chamado wishListRepository.findAll E deve"
          + " devolver uma lista de ClientWishList")
  void getAllWishLists() {

    when(wishListRepository.findAll()).thenReturn(Collections.emptyList());

    List<ClientWishList> allClientWishLists = wishListServiceImpl.getAllWishLists();

    verify(wishListRepository, times(1)).findAll();

    assertNotNull(allClientWishLists);
  }*/

  @SneakyThrows
  @Test
  @DisplayName(
      "QUANDO o metodo addProductToWishList for chamado com um clientId e um productId ENTAO deve salvar e retornar o"
          + " ClientWishList salvo")
  void addProductToWishListUsingClientHaveWishList() {

    AddProductToWishListDto addProductToWishListDto =
        AddProductToWishListDto.builder().clientId(123L).productId(321L).build();

    when(wishListRepository.save(any())).thenReturn(ClientWishList.builder().build());

    ClientWishList clientWishList =
        wishListServiceImpl.addProductToWishList(
            addProductToWishListDto.getClientId(), addProductToWishListDto.getProductId());

    verify(wishListRepository, times(1)).save(any());
    assertNotNull(clientWishList);
  }

  @SneakyThrows
  @Test
  @DisplayName(
      "QUANDO buildClientWishListFrom receber um clientId e um productId E o clientId nao "
          + " tem um ClientWishList ENTAO deve criar uma ClientWishList nova")
  void buildClientWishListClientIdNotHaveWishList() {

    Long productIdToAdd = 321L;
    Long clientId = 123L;

    AddProductToWishListDto addProductToWishListDto =
        AddProductToWishListDto.builder().clientId(clientId).productId(productIdToAdd).build();

    ClientWishList clientWishList =
        wishListServiceImpl.buildClientWishListFrom(
            addProductToWishListDto.getClientId(), addProductToWishListDto.getProductId());

    ZonedDateTime createdAt =
        ZonedDateTime.parse(clientWishList.getCreatedAt(), DateTimeFormatter.ISO_ZONED_DATE_TIME);

    verify(wishListRepository, times(1)).findByClientId(clientId);
    assertTrue(createdAt.isBefore(ZonedDateTime.now()));
    assertEquals(1, clientWishList.getWishList().getTotalProductionInList());
    assertEquals(123L, clientWishList.getClientId());
    assertEquals(321L, clientWishList.getWishList().getProductsIds().get(0));
  }

  @SneakyThrows
  @Test
  @DisplayName(
      "QUANDO buildClientWishListFrom receber um clientId e um productId E o clientId ja tem "
          + " um ClientWishList ENTAO deve adicionar o produto a ClientWishList existente")
  void buildClientWishListClientIdHaveWishList() {

    Long productIdToAdd = 321L;
    Long clientId = 123L;
    ArrayList<Long> clientOthersProductIds = new ArrayList<>(List.of(789L));
    Optional<ClientWishList> clientWishListOnDataBase =
        Optional.of(
            ClientWishList.builder()
                .clientId(clientId)
                .wishList(WishList.builder().productsIds(clientOthersProductIds).build())
                .createdAt("2023-05-27T00:00:00-03:00")
                .build());

    when(wishListRepository.findByClientId(clientId)).thenReturn(clientWishListOnDataBase);
    setupMaxItemsInWishList();

    AddProductToWishListDto addProductToWishListDto =
        AddProductToWishListDto.builder().clientId(clientId).productId(productIdToAdd).build();

    ClientWishList clientWishList =
        wishListServiceImpl.buildClientWishListFrom(
            addProductToWishListDto.getClientId(), addProductToWishListDto.getProductId());

    ZonedDateTime createdAt =
        ZonedDateTime.parse(clientWishList.getCreatedAt(), DateTimeFormatter.ISO_ZONED_DATE_TIME);

    verify(wishListRepository, times(1)).findByClientId(clientId);
    assertTrue(createdAt.isBefore(ZonedDateTime.now()));
    assertEquals(2, clientWishList.getWishList().getTotalProductionInList());
    assertEquals(123L, clientWishList.getClientId());
    assertTrue(clientWishList.getWishList().getProductsIds().contains(321L));
    assertTrue(clientWishList.getWishList().getProductsIds().contains(789L));
  }

  @Test
  @DisplayName(
      "QUANDO buildClientWishListFrom receber um clientId e um productId E o clientId ja tem "
          + " tem um ClientWishList e a lista ja alcançou o máximo de produtos permitidos ENTAO "
          + " deve retornar erro")
  void buildClientWishListClientIdHaveMaxProductsOnWishList() {
    Long productIdToAdd = 321L;
    Long clientId = 123L;
    ArrayList<Long> clientOthersProductIds = new ArrayList<>(Collections.nCopies(20, 1L));
    Optional<ClientWishList> clientWishListOnDataBase =
        Optional.of(
            ClientWishList.builder()
                .clientId(clientId)
                .wishList(WishList.builder().productsIds(clientOthersProductIds).build())
                .createdAt("2023-05-27T00:00:00-03:00")
                .build());

    when(wishListRepository.findByClientId(clientId)).thenReturn(clientWishListOnDataBase);
    setupMaxItemsInWishList();

    AddProductToWishListDto addProductToWishListDto =
        AddProductToWishListDto.builder().clientId(clientId).productId(productIdToAdd).build();

    assertThrows(
        MaxProductsInClientWishListException.class,
        () -> {
          wishListServiceImpl.buildClientWishListFrom(
              addProductToWishListDto.getClientId(), addProductToWishListDto.getProductId());
        });
  }

  @Test
  @DisplayName(
      "QUANDO buildClientWishListFrom receber um clientId e um productId E o productId ja existe "
          + " em ClientWishList ENTAO deve retornar erro")
  void buildClientWishListClientIdAlreadyHaveProductInWishList() {
    Long productIdToAddAlreadyExist = 321L;
    Long clientId = 123L;
    ArrayList<Long> clientOthersProductIds = new ArrayList<>(Collections.nCopies(1, 321L));
    Optional<ClientWishList> clientWishListOnDataBase =
        Optional.of(
            ClientWishList.builder()
                .clientId(clientId)
                .wishList(WishList.builder().productsIds(clientOthersProductIds).build())
                .createdAt("2023-05-27T00:00:00-03:00")
                .build());

    when(wishListRepository.findByClientId(clientId)).thenReturn(clientWishListOnDataBase);
    setupMaxItemsInWishList();

    AddProductToWishListDto addProductToWishListDto =
        AddProductToWishListDto.builder()
            .clientId(clientId)
            .productId(productIdToAddAlreadyExist)
            .build();

    assertThrows(
        ProductAlreadyExistsInWishListException.class,
        () -> {
          wishListServiceImpl.buildClientWishListFrom(
              addProductToWishListDto.getClientId(), addProductToWishListDto.getProductId());
        });
  }

  @SneakyThrows
  @Test
  @DisplayName(
      "QUANDO removeProductFromWishList um clientId e productId validos ENTAO deve remover o productId da clientWishList")
  void removeProductFromWishList() {

    Long clientId = 123L;

    ArrayList<Long> clientOthersProductIds = new ArrayList<>(Collections.nCopies(1, 321L));

    Optional<ClientWishList> clientWishListOnDataBase =
        Optional.of(
            ClientWishList.builder()
                .clientId(clientId)
                .wishList(WishList.builder().productsIds(clientOthersProductIds).build())
                .build());

    when(wishListRepository.findByClientId(clientId)).thenReturn(clientWishListOnDataBase);

    wishListServiceImpl.removeProductFromWishList(clientId, 321L);

    verify(wishListRepository, times(1)).save(any());
  }

  @Test
  @DisplayName(
      "QUANDO removeProductFromWishList receber um RemoveProductFromWishListDto que tem um clientId que nao existe ENTAO"
          + " deve gerar ClientNotFoundException")
  void removeProductFromWishListWithInvalidClientId() {
    Long clientIdDoesNotExist = 123L;

    Optional<ClientWishList> clientWishListOnDataBase = Optional.empty();

    when(wishListRepository.findByClientId(clientIdDoesNotExist))
        .thenReturn(clientWishListOnDataBase);

    assertThrows(
        ClientNotFoundException.class,
        () -> {
          wishListServiceImpl.removeProductFromWishList(clientIdDoesNotExist, 321L);
        });

    verify(wishListRepository, times(0)).delete(any());
  }

  @Test
  @DisplayName(
      "QUANDO removeProductFromWishList receber um client id e um productId que nao existe na"
          + " lista do clientId ENTAO gerar ProductNotFoundInWishListException")
  void removeProductFromWishListWithInvalidProductId() {
    Long clientId = 123L;
    Long productIdNotExist = 456L;

    ArrayList<Long> clientOthersProductIds = new ArrayList<>(Collections.nCopies(1, 321L));

    Optional<ClientWishList> clientWishListOnDataBase =
        Optional.of(
            ClientWishList.builder()
                .clientId(clientId)
                .wishList(WishList.builder().productsIds(clientOthersProductIds).build())
                .build());

    when(wishListRepository.findByClientId(clientId)).thenReturn(clientWishListOnDataBase);

    assertThrows(
        ProductNotFoundInWishListException.class,
        () -> {
          wishListServiceImpl.removeProductFromWishList(clientId, productIdNotExist);
        });

    verify(wishListRepository, times(0)).delete(any());
  }

  @SneakyThrows
  @Test
  @DisplayName(
      "QUANDO o metodo getWishListProductsByClientId for chamado com um client valido ENTAO deve ser chamado "
          + "wishListRepository.findByClientId E deve retornar os produtos di ClientWishList")
  void getWishListProductsByClientId() {

    Long clientId = 123L;

    when(wishListRepository.findByClientId(clientId))
        .thenReturn(
            Optional.of(
                ClientWishList.builder()
                    .clientId(clientId)
                    .wishList(
                        WishList.builder().productsIds(Collections.singletonList(123L)).build())
                    .build()));

    List<Long> wishListProductsByClientId =
        wishListServiceImpl.getWishListProductsByClientId(clientId);

    verify(wishListRepository, times(1)).findByClientId(clientId);

    assertEquals(1, wishListProductsByClientId.size());
  }

  @Test
  @DisplayName(
      "QUANDO o metodo getWishListProductsByClientId for chamado com um clientId invalido ENTAO deve ser gerada uma "
          + "ClientNotFoundException")
  void getWishListProductsByInvalidClientId() {

    Long clientId = 123L;

    when(wishListRepository.findByClientId(clientId)).thenReturn(Optional.empty());

    ClientNotFoundException clientNotFoundException =
        assertThrows(
            ClientNotFoundException.class,
            () -> {
              wishListServiceImpl.getWishListProductsByClientId(clientId);
            });

    verify(wishListRepository, times(1)).findByClientId(clientId);
    assertEquals("Client id 123 not found", clientNotFoundException.getMessage());
  }

  @SneakyThrows
  @Test
  @DisplayName(
      "QUANDO o metodo getWishListProductByClientId for chamado com um client valido e product id valido ENTAO "
          + "deve ser chamado wishListRepository.findByClientId E deve retornar apenas o produtos id solicitado")
  void getWishListProductByClientId() {

    Long clientId = 123L;
    Long productId = 321L;

    when(wishListRepository.findByClientId(clientId))
        .thenReturn(
            Optional.of(
                ClientWishList.builder()
                    .clientId(clientId)
                    .wishList(WishList.builder().productsIds(List.of(productId, 456L)).build())
                    .build()));

    Long productIdFound = wishListServiceImpl.getWishListProductByClientId(clientId, productId);

    verify(wishListRepository, times(1)).findByClientId(clientId);

    assertEquals(321L, productIdFound);
  }
}
