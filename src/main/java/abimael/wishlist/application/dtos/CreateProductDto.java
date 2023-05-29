package abimael.wishlist.application.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;

public class CreateProductDto {

  @NotBlank(message = "Product name cannot be empty")
  @Length(min = 5, message = "Product name must have a minimum length of 5 characters")
  private String name;

  @NotBlank(message = "Product description cannot be empty")
  @Length(min = 10, message = "Product description must have a minimum length of 10 characters")
  private String description;

  @Min(value = 0, message = "Product value must have greater or equals 0")
  private BigDecimal price;
}
