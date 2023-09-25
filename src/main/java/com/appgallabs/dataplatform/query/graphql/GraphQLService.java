package com.appgallabs.dataplatform.query.graphql;

import org.eclipse.microprofile.graphql.*;

import javax.inject.Inject;
import java.util.List;

@GraphQLApi
public class GraphQLService {

    @Inject
    PersonService personService;

    @Inject
    private ProductService productService;

    @Inject
    private QueryService queryService;

    @Query("all")
    @Description("Get all records.")
    public List<DataLakeRecord> getAll() {
        return this.queryService.allRecords();
    }

    @Query("allProducts")
    @Description("Get all products.")
    public List<ProductDTO> getAllProducts() {
        return this.productService.allProducts();
    }

    @Query("allCountries")
    @Description("Get all countries.")
    public List<Country> getAllCountries() {
        return personService.getAllCountries();
    }

    @Query
    @Description("Get a Country.")
    public Country getCountry(@Name("countryId") int id) {
        return personService.getCountry(id);
    }

    @Query("allPersons")
    @Description("Get all persons.")
    public List<Person> getAllPersons() {
        return personService.getAllPersons();
    }


    @Query
    @Description("Get a Person")
    public Person getPerson(@Name("personId") int id) {
        return personService.getPerson(id);
    }

    public List<Person> persons(@Source Country country) {
        return personService.getPersonByCity(country);
    }
    @Mutation
    public Person createPerson(@Name("person") Person person) {
        personService.addPerson(person);
        return person;
    }

}
