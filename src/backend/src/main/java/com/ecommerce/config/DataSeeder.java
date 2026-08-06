package com.ecommerce.config;

import com.ecommerce.domain.*;
import com.ecommerce.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
@Profile("test")
public class DataSeeder {

    @Bean
    CommandLineRunner seed(CategoryRepository categoryRepo, ProductRepository productRepo,
                           ProductImageRepository imageRepo, ProductVariantRepository variantRepo,
                           CustomerRepository customerRepo, AddressRepository addressRepo,
                           CartItemRepository cartRepo) {
        return args -> {
            Category electronics = new Category();
            electronics.setName("Electronics");
            electronics.setDescription("Electronic gadgets");
            electronics.setThumbnailUrl("/images/electronics.jpg");
            categoryRepo.save(electronics);

            Category phones = new Category();
            phones.setName("Phones");
            phones.setDescription("Smartphones");
            phones.setThumbnailUrl("/images/phones.jpg");
            phones.setParentCategoryId(electronics.getId());
            categoryRepo.save(phones);

            Category accessories = new Category();
            accessories.setName("Accessories");
            accessories.setDescription("Phone accessories");
            accessories.setThumbnailUrl("/images/accessories.jpg");
            accessories.setParentCategoryId(electronics.getId());
            categoryRepo.save(accessories);

            Category clothing = new Category();
            clothing.setName("Clothing");
            clothing.setDescription("Apparel");
            clothing.setThumbnailUrl("/images/clothing.jpg");
            categoryRepo.save(clothing);

            Product mouse = new Product();
            mouse.setName("Wireless Mouse");
            mouse.setDescription("Ergonomic wireless mouse");
            mouse.setPrice(new BigDecimal("999.00"));
            mouse.setCategoryId(electronics.getId());
            mouse.setImageUrl("/images/mouse.jpg");
            mouse.setThumbnailUrl("/images/mouse-thumb.jpg");
            mouse.setIsActive(true);
            mouse.setCreatedDate(LocalDateTime.now());
            productRepo.save(mouse);

            Product iphone = new Product();
            iphone.setName("iPhone 15");
            iphone.setDescription("Latest Apple smartphone");
            iphone.setPrice(new BigDecimal("79999.00"));
            iphone.setCategoryId(phones.getId());
            iphone.setImageUrl("/images/iphone.jpg");
            iphone.setThumbnailUrl("/images/iphone-thumb.jpg");
            iphone.setIsActive(true);
            iphone.setCreatedDate(LocalDateTime.now());
            productRepo.save(iphone);

            Product cable = new Product();
            cable.setName("USB-C Cable");
            cable.setDescription("Fast charging cable");
            cable.setPrice(new BigDecimal("499.00"));
            cable.setCategoryId(accessories.getId());
            cable.setImageUrl("/images/cable.jpg");
            cable.setThumbnailUrl("/images/cable-thumb.jpg");
            cable.setIsActive(true);
            cable.setCreatedDate(LocalDateTime.now());
            productRepo.save(cable);

            Product tshirt = new Product();
            tshirt.setName("T-Shirt");
            tshirt.setDescription("Cotton t-shirt");
            tshirt.setPrice(new BigDecimal("699.00"));
            tshirt.setCategoryId(clothing.getId());
            tshirt.setImageUrl("/images/tshirt.jpg");
            tshirt.setThumbnailUrl("/images/tshirt-thumb.jpg");
            tshirt.setIsActive(true);
            tshirt.setCreatedDate(LocalDateTime.now());
            productRepo.save(tshirt);

            Product old = new Product();
            old.setName("Old Gadget");
            old.setDescription("Discontinued");
            old.setPrice(new BigDecimal("100.00"));
            old.setCategoryId(electronics.getId());
            old.setImageUrl("/images/old.jpg");
            old.setThumbnailUrl("/images/old-thumb.jpg");
            old.setIsActive(false);
            old.setCreatedDate(LocalDateTime.now());
            productRepo.save(old);

            ProductImage img1 = new ProductImage();
            img1.setProduct(mouse);
            img1.setImageUrl("/images/mouse-1.jpg");
            img1.setIsPrimary(true);
            img1.setDisplayOrder(0);
            imageRepo.save(img1);

            ProductImage img2 = new ProductImage();
            img2.setProduct(mouse);
            img2.setImageUrl("/images/mouse-2.jpg");
            img2.setIsPrimary(false);
            img2.setDisplayOrder(1);
            imageRepo.save(img2);

            ProductImage img3 = new ProductImage();
            img3.setProduct(iphone);
            img3.setImageUrl("/images/iphone-1.jpg");
            img3.setIsPrimary(true);
            img3.setDisplayOrder(0);
            imageRepo.save(img3);

            ProductVariant v128 = new ProductVariant();
            v128.setProduct(iphone);
            v128.setVariantName("Storage");
            v128.setVariantValue("128GB");
            v128.setPriceAdjustment(BigDecimal.ZERO);
            v128.setStockQuantity(50);
            variantRepo.save(v128);

            ProductVariant v256 = new ProductVariant();
            v256.setProduct(iphone);
            v256.setVariantName("Storage");
            v256.setVariantValue("256GB");
            v256.setPriceAdjustment(new BigDecimal("10000"));
            v256.setStockQuantity(30);
            variantRepo.save(v256);

            Customer customer = new Customer();
            customer.setEmail("test@example.com");
            customer.setFirstName("Test");
            customer.setLastName("User");
            customer.setPhoneNumber("9876543210");
            customer.setCreatedDate(LocalDateTime.now());
            customerRepo.save(customer);

            Address addr = new Address();
            addr.setCustomerId(customer.getId());
            addr.setAddressType("Shipping");
            addr.setStreetAddress("123 Main St");
            addr.setCity("Mumbai");
            addr.setStateProvince("MH");
            addr.setPostalCode("400001");
            addr.setCountry("India");
            addr.setIsDefault(true);
            addressRepo.save(addr);

            CartItem cart = new CartItem();
            cart.setCustomerId(customer.getId());
            cart.setProductId(iphone.getId());
            cart.setVariantId(v128.getId());
            cart.setQuantity(1);
            cart.setUnitPrice(iphone.getPrice());
            cart.setCreatedDate(LocalDateTime.now());
            cartRepo.save(cart);
        };
    }
}
