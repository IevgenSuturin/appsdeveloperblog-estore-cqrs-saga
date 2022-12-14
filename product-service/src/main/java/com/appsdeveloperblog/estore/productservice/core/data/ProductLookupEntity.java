package com.appsdeveloperblog.estore.productservice.core.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "productLookup")
public class ProductLookupEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String productId;

    @Column(unique = true)
    private String title;
}
