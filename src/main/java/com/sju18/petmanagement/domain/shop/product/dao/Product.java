package com.sju18.petmanagement.domain.shop.product.dao;

import com.sju18.petmanagement.domain.map.place.dao.Place;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;
    @Column(nullable = false)
    private String name;
    @ManyToOne(targetEntity = Place.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "place_id", foreignKey = @ForeignKey(
            name = "fk_product_place_id",
            foreignKeyDefinition = "FOREIGN KEY (place_id) REFERENCES place (place_id) ON DELETE CASCADE"
    ))
    private Place place;

    @Column(nullable = false)
    private String type;
    @Column(nullable = false)
    private Long price;
    @Column(nullable = false)
    private Boolean prepaid;
    @Column(nullable = false)
    private Long stock;
    @Column(nullable = false)
    private LocalDateTime timestamp;
    @Column(nullable = false)
    private String status;
    @Column(nullable = false)
    private Boolean visible;
    @Lob
    private String description;
    @Column
    private String shippingType;
    private Long shippingPrice;
    private Long reservationPrice;
    private String returnPolicy;
    private String photoUrl;
}
