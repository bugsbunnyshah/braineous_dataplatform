package com.appgallabs.dataplatform.query.graphql;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProductService {

    private List<ProductDTO> products = new ArrayList();


    public ProductService() {
        ProductDTO p1 = new ProductDTO();
        p1.setName("p1");

        products.add(p1);
    }

    public List<ProductDTO> allProducts() {
        return this.products;
    }
}
