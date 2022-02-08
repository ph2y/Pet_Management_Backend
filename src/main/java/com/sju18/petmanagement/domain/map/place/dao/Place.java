package com.sju18.petmanagement.domain.map.place.dao;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Place {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "place_id")
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String categoryCode;
    @Column(nullable = false)
    private Double latitude;
    @Column(nullable = false)
    private Double longitude;
    @Lob
    private String description;
    @Column
    private Double averageRating;
    private String phone;
    private String operationHour;
}
