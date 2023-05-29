package abimael.wishlist.application.controllers;

import abimael.wishlist.application.dtos.AddProductToWishListDto;
import abimael.wishlist.application.dtos.ApiResponseDto;
import abimael.wishlist.application.dtos.RemoveProductFromWishListDto;
import abimael.wishlist.domain.models.ClientWishList;
import abimael.wishlist.infrastructure.services.WishListService;
import abimael.wishlist.infrastructure.services.exceptions.ClientNotFoundException;
import abimael.wishlist.infrastructure.services.exceptions.MaxProductsInClientWishListException;
import abimael.wishlist.infrastructure.services.exceptions.ProductAlreadyExistsInWishListException;
import abimael.wishlist.infrastructure.services.exceptions.ProductNotFoundInWishListException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@AllArgsConstructor
@Slf4j
public class WishListController {

  private final WishListService wishListService;

  @PostMapping
  public ResponseEntity<ApiResponseDto<ClientWishList>> addProductToWishList(
      @RequestBody @Validated AddProductToWishListDto addProductToWishListDto) {

    ApiResponseDto<ClientWishList> apiResponse = ApiResponseDto.<ClientWishList>builder().build();

    try {
      return apiResponse.buildOkResponse(
          wishListService.addProductToWishList(
              addProductToWishListDto.getClientId(), addProductToWishListDto.getProductId()));
    } catch (MaxProductsInClientWishListException | ProductAlreadyExistsInWishListException e) {
      return apiResponse.buildBadRequestResponse(e.getMessage());
    } catch (Exception e) {
      log.error(e.getMessage());
      return apiResponse.buildIntervalServerErrorResponse();
    }
  }

  @DeleteMapping("client/{clientId}/product/{productId}")
  public ResponseEntity<ApiResponseDto<RemoveProductFromWishListDto>> removeProductFromWishList(
      @PathVariable Long clientId, @PathVariable Long productId) {

    ApiResponseDto<RemoveProductFromWishListDto> apiResponse =
        ApiResponseDto.<RemoveProductFromWishListDto>builder().build();

    try {
      wishListService.removeProductFromWishList(clientId, productId);
      return apiResponse.buildNoContentResponse();
    } catch (ClientNotFoundException | ProductNotFoundInWishListException e) {
      return apiResponse.buildBadRequestResponse(e.getMessage());

    } catch (Exception e) {
      log.error(e.getMessage());
      return apiResponse.buildIntervalServerErrorResponse();
    }
  }

  @GetMapping("/client/{clientId}/products")
  public ResponseEntity<ApiResponseDto<List<Long>>> getWishListProductsByClientId(
      @PathVariable Long clientId) {
    ApiResponseDto<List<Long>> apiResponse = ApiResponseDto.<List<Long>>builder().build();

    try {
      return apiResponse.buildOkResponse(wishListService.getWishListProductsByClientId(clientId));
    } catch (ClientNotFoundException e) {
      return apiResponse.buildNotFoundRequestResponse(e.getMessage());
    } catch (Exception e) {
      log.error(e.getMessage());
      return apiResponse.buildIntervalServerErrorResponse();
    }
  }

  @GetMapping("/client/{clientId}/product/{productId}")
  public ResponseEntity<ApiResponseDto<Long>> getWishListProductByClientId(
      @PathVariable Long clientId, @PathVariable Long productId) {

    ApiResponseDto<Long> apiResponse = ApiResponseDto.<Long>builder().build();
    try {
      return apiResponse.buildOkResponse(
          wishListService.getWishListProductByClientId(clientId, productId));
    } catch (ClientNotFoundException | ProductNotFoundInWishListException e) {
      return apiResponse.buildNotFoundRequestResponse(e.getMessage());
    } catch (Exception e) {
      log.error(e.getMessage());
      return apiResponse.buildIntervalServerErrorResponse();
    }
  }
}
