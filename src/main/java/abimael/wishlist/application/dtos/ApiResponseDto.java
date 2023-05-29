package abimael.wishlist.application.dtos;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.ResponseEntity;

@Builder
@Getter
public class ApiResponseDto<T> {

  private String message;
  private T data;

  public ResponseEntity<ApiResponseDto<T>> buildOkResponse(T data) {
    return ResponseEntity.ok().body(ApiResponseDto.<T>builder().data(data).build());
  }

  public ResponseEntity<ApiResponseDto<T>> buildIntervalServerErrorResponse() {
    return ResponseEntity.internalServerError()
        .body(ApiResponseDto.<T>builder().message("An unexpected error occurred").build());
  }

  public ResponseEntity<ApiResponseDto<T>> buildBadRequestResponse(String message) {
    return ResponseEntity.badRequest().body(ApiResponseDto.<T>builder().message(message).build());
  }

  public ResponseEntity<ApiResponseDto<T>> buildNotFoundRequestResponse(String message) {
    return ResponseEntity.notFound().build();
  }

  public ResponseEntity<ApiResponseDto<T>> buildNoContentResponse() {
    return ResponseEntity.noContent().build();
  }
}
