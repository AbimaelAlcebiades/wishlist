package abimael.wishlist.application.controllers;

import abimael.wishlist.application.dtos.AddProductToWishListDto;
import abimael.wishlist.application.dtos.ApiResponseDto;
import abimael.wishlist.application.dtos.RemoveProductFromWishListDto;
import abimael.wishlist.domain.models.ClientWishList;
import abimael.wishlist.infrastructure.services.WishListService;
import abimael.wishlist.infrastructure.services.exceptions.*;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishListControllerTest {

  @Mock private WishListService wishListService;

  @InjectMocks private WishListController wishListController;

  /*@SneakyThrows
  @Test
  @DisplayName(
      "QUANDO o metodo getAllWishLists for chamado ENTAO deve ser chamado ProductService.getAllWishLists retornando"
          + " todos os wishlists de todos os clientes")
  void getAllWishLists() {

    List<ClientWishList> clientWishLists =
        Collections.singletonList(ClientWishList.builder().id("12345").build());

    when(wishListService.getAllWishLists()).thenReturn(clientWishLists);

    ResponseEntity<ApiResponseDto<List<ClientWishList>>> apiResponseDtoResponseEntity =
        wishListController.getAllWishLists();

    List<ClientWishList> allClientWishLists =
        Objects.requireNonNull(apiResponseDtoResponseEntity.getBody()).getData();

    verify(wishListService, times(1)).getAllWishLists();
    assertEquals("12345", allClientWishLists.get(0).getId());
  }*/

  @SneakyThrows
  @Test
  @DisplayName(
      "QUANDO o metodo addProductToWishList for chamado com um clientId e productId ENTAO deve ser chamado"
          + " ProductService.addProductToWishList recebendo ddProductToWishListDto como argumento "
          + "E deve devolver uma ClientWishList")
  void addProductToWishList() {

    AddProductToWishListDto addProductToWishListDto =
        AddProductToWishListDto.builder().clientId(123L).productId(321L).build();

    when(wishListService.addProductToWishList(
            addProductToWishListDto.getClientId(), addProductToWishListDto.getProductId()))
        .thenReturn(ClientWishList.builder().build());

    ResponseEntity<ApiResponseDto<ClientWishList>> apiResponseDtoResponseEntity =
        wishListController.addProductToWishList(addProductToWishListDto);

    ClientWishList clientWishList =
        Objects.requireNonNull(apiResponseDtoResponseEntity.getBody()).getData();

    verify(wishListService, times(1))
        .addProductToWishList(
            addProductToWishListDto.getClientId(), addProductToWishListDto.getProductId());
    assertEquals(HttpStatus.OK, apiResponseDtoResponseEntity.getStatusCode());
    assertNotNull(clientWishList);
  }

  @SneakyThrows
  @Test
  @DisplayName(
      "QUANDO o metodo addProductToWishList for chamado com um clientId e productId E o numero maximo de itens no "
          + "clientWishList ja foi excedido ENTAO deve gerar excecao E retornar uma mensagem de erro com corpo null")
  void addProductToWishListMaxItemsExceeded() {

    AddProductToWishListDto addProductToWishListDto =
        AddProductToWishListDto.builder().clientId(123L).productId(321L).build();

    when(wishListService.addProductToWishList(
            addProductToWishListDto.getClientId(), addProductToWishListDto.getProductId()))
        .thenThrow(new MaxProductsInClientWishListException("Message exceeded items"));

    ResponseEntity<ApiResponseDto<ClientWishList>> apiResponseDtoResponseEntity =
        wishListController.addProductToWishList(addProductToWishListDto);

    ApiResponseDto<ClientWishList> body =
        Objects.requireNonNull(apiResponseDtoResponseEntity.getBody());

    verify(wishListService, times(1))
        .addProductToWishList(
            addProductToWishListDto.getClientId(), addProductToWishListDto.getProductId());
    assertEquals(HttpStatus.BAD_REQUEST, apiResponseDtoResponseEntity.getStatusCode());
    assertNull(body.getData());
    assertEquals("Message exceeded items", body.getMessage());
  }

  @SneakyThrows
  @Test
  @DisplayName(
      "QUANDO o metodo addProductToWishList for chamado com um clientId e productId E o produto ja existe no "
          + " clientWishList ENTAO deve gerar excecao E retornar HTTP code 400 E uma mensagem de erro E corpo null")
  void addProductAlreadyExistsInWishList() {

    AddProductToWishListDto addProductToWishListDto = AddProductToWishListDto.builder().build();

    when(wishListService.addProductToWishList(
            addProductToWishListDto.getClientId(), addProductToWishListDto.getProductId()))
        .thenThrow(
            new ProductAlreadyExistsInWishListException("Message item already exists in wishlist"));

    ResponseEntity<ApiResponseDto<ClientWishList>> apiResponseDtoResponseEntity =
        wishListController.addProductToWishList(addProductToWishListDto);

    ApiResponseDto<ClientWishList> body =
        Objects.requireNonNull(apiResponseDtoResponseEntity.getBody());

    verify(wishListService, times(1))
        .addProductToWishList(
            addProductToWishListDto.getClientId(), addProductToWishListDto.getProductId());
    assertEquals(HttpStatus.BAD_REQUEST, apiResponseDtoResponseEntity.getStatusCode());
    assertNull(body.getData());
    assertEquals("Message item already exists in wishlist", body.getMessage());
  }

  @SneakyThrows
  @Test
  @DisplayName(
      "QUANDO o metodo removeProductToWishList for chamado com um clientId e productId E o productId existir na "
          + "clientWishList ENTAO deve remover o item e devolver HTTP code 204")
  void removeProductThatExistsInWishList() {

    Long clientId = 123L;
    Long productId = 321L;

    ResponseEntity<ApiResponseDto<RemoveProductFromWishListDto>> apiResponseDtoResponseEntity =
        wishListController.removeProductFromWishList(clientId, productId);

    verify(wishListService, times(1)).removeProductFromWishList(clientId, productId);
    assertNull(apiResponseDtoResponseEntity.getBody());
    assertEquals(HttpStatus.NO_CONTENT, apiResponseDtoResponseEntity.getStatusCode());
  }

  @SneakyThrows
  @Test
  @DisplayName(
      "QUANDO o metodo removeProductToWishList for chamado com um clientId e productId E o productId nao existir na "
          + "clientWishList ENTAO retornar HTTP code 400 e uma mensagem de erro")
  void removeProductThatDoesNotExistsInWishList() {

    Long clientId = 123L;
    Long productId = 321L;

    Mockito.doThrow(new ProductNotFoundInWishListException("Product does not exists"))
        .when(wishListService)
        .removeProductFromWishList(clientId, productId);

    ResponseEntity<ApiResponseDto<RemoveProductFromWishListDto>> apiResponseDtoResponseEntity =
        wishListController.removeProductFromWishList(clientId, productId);

    ApiResponseDto<RemoveProductFromWishListDto> body =
        Objects.requireNonNull(apiResponseDtoResponseEntity.getBody());

    verify(wishListService, times(1)).removeProductFromWishList(clientId, productId);
    assertEquals(HttpStatus.BAD_REQUEST, apiResponseDtoResponseEntity.getStatusCode());
    assertEquals("Product does not exists", body.getMessage());
  }

  @SneakyThrows
  @Test
  @DisplayName(
      "QUANDO o metodo getWishListProductsByClientId for chamado com um clientId valido ENTAO deve retornar "
          + " HTTP code 200 e a lista de productIds")
  void getWishListProductsByClientId() {

    Long clientId = 123L;

    when(wishListService.getWishListProductsByClientId(clientId))
        .thenReturn(Collections.singletonList(123L));

    ResponseEntity<ApiResponseDto<List<Long>>> apiResponseDtoResponseEntity =
        wishListController.getWishListProductsByClientId(clientId);

    verify(wishListService, times(1)).getWishListProductsByClientId(clientId);
    assertEquals(HttpStatus.OK, apiResponseDtoResponseEntity.getStatusCode());
    assertEquals(
        1, Objects.requireNonNull(apiResponseDtoResponseEntity.getBody()).getData().size());
  }

  @SneakyThrows
  @Test
  @DisplayName(
      "QUANDO o metodo getWishListProductsByClientId for chamado com um clientId invalido ENTAO deve retornar "
          + " HTTP code 400 e mensagem de erro e sem dados")
  void getWishListProductsByNotFoundClientId() {

    Long clientId = 123L;

    when(wishListService.getWishListProductsByClientId(clientId))
        .thenThrow(new ClientNotFoundException("Client not found"));

    ResponseEntity<ApiResponseDto<List<Long>>> apiResponseDtoResponseEntity =
        wishListController.getWishListProductsByClientId(clientId);

    verify(wishListService, times(1)).getWishListProductsByClientId(clientId);
    assertEquals(HttpStatus.NOT_FOUND, apiResponseDtoResponseEntity.getStatusCode());
  }

  @SneakyThrows
  @Test
  @DisplayName(
      "QUANDO o metodo getWishListProductsByClientId for chamado com um clientId E ocorrer erro na wishListService"
          + " ENTAO deve retornar 500 e mensagem de erro")
  void getWishListProductsByClientIdServiceError() {

    Long clientId = 123L;

    when(wishListService.getWishListProductsByClientId(clientId))
        .thenThrow(new ServiceException("Any untrack service error"));

    ResponseEntity<ApiResponseDto<List<Long>>> apiResponseDtoResponseEntity =
        wishListController.getWishListProductsByClientId(clientId);

    verify(wishListService, times(1)).getWishListProductsByClientId(clientId);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, apiResponseDtoResponseEntity.getStatusCode());
  }

  @SneakyThrows
  @Test
  @DisplayName(
      "QUANDO o metodo getWishListProductByClientId for chamado com um clientId valido e um produto valido ENTAO "
          + "deve retornar HTTP code 200 e o produto")
  void getWishListProductByClientId() {

    Long clientId = 123L;
    Long productId = 321L;

    when(wishListService.getWishListProductByClientId(clientId, productId)).thenReturn(321L);

    ResponseEntity<ApiResponseDto<Long>> apiResponseDtoResponseEntity =
        wishListController.getWishListProductByClientId(clientId, productId);

    verify(wishListService, times(1)).getWishListProductByClientId(clientId, productId);
    assertEquals(HttpStatus.OK, apiResponseDtoResponseEntity.getStatusCode());
    assertEquals(321L, Objects.requireNonNull(apiResponseDtoResponseEntity.getBody()).getData());
  }

  @SneakyThrows
  @Test
  @DisplayName(
      "QUANDO o metodo getWishListProductByClientId for chamado com um clientId invalido ou um productId invalido "
          + " ENTAO deve retornar 404")
  void getWishListProductByNotFoundClientIdOrProductId() {

    Long clientIdNotExists = 123L;
    Long productId = 456L;

    Long clientId = 789L;
    Long productIdNotExists = 101L;

    when(wishListService.getWishListProductByClientId(clientIdNotExists, productId))
        .thenThrow(new ClientNotFoundException("Client not found"));

    when(wishListService.getWishListProductByClientId(clientId, productIdNotExists))
        .thenThrow(new ClientNotFoundException("Product not found"));

    ResponseEntity<ApiResponseDto<Long>> apiResponseDtoResponseEntityProductNotFound =
        wishListController.getWishListProductByClientId(clientId, productIdNotExists);

    ResponseEntity<ApiResponseDto<Long>> apiResponseDtoResponseEntityClientNotFound =
        wishListController.getWishListProductByClientId(clientIdNotExists, productId);

    verify(wishListService, times(1)).getWishListProductByClientId(clientIdNotExists, productId);
    verify(wishListService, times(1)).getWishListProductByClientId(clientId, productIdNotExists);
    assertEquals(HttpStatus.NOT_FOUND, apiResponseDtoResponseEntityClientNotFound.getStatusCode());
    assertEquals(HttpStatus.NOT_FOUND, apiResponseDtoResponseEntityProductNotFound.getStatusCode());
  }

  @SneakyThrows
  @Test
  @DisplayName(
      "QUANDO o metodo getWishListProductByClientId for chamado E ocorrer erro na wishListService"
          + " ENTAO deve retornar 500 e mensagem de erro")
  void getWishListProductByClientIdServiceError() {

    Long clientId = 123L;
    Long productId = 321L;

    when(wishListService.getWishListProductByClientId(clientId, productId))
        .thenThrow(new ServiceException("Any untrack service error"));

    ResponseEntity<ApiResponseDto<Long>> apiResponseDtoResponseEntity =
        wishListController.getWishListProductByClientId(clientId, productId);

    verify(wishListService, times(1)).getWishListProductByClientId(clientId, productId);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, apiResponseDtoResponseEntity.getStatusCode());
  }
}
