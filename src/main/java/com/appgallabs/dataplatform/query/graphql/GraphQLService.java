package com.appgallabs.dataplatform.query.graphql;

import com.appgallabs.dataplatform.preprocess.SecurityToken;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.graphql.*;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.List;

@GraphQLApi
public class GraphQLService {

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Inject
    PersonService personService;

    @Inject
    private ProductService productService;

    @Inject
    private QueryService queryService;

    private void setSecurityToken(){
        try {
            String credentials = IOUtils.resourceToString("oauth/credentials.json",
                    StandardCharsets.UTF_8,
                    Thread.currentThread().getContextClassLoader());
            JsonObject credentialsJson = JsonParser.parseString(credentials).getAsJsonObject();
            String token = IOUtils.resourceToString("oauth/jwtToken.json",
                    StandardCharsets.UTF_8,
                    Thread.currentThread().getContextClassLoader());
            JsonObject securityTokenJson = JsonParser.parseString(token).getAsJsonObject();
            securityTokenJson.addProperty("principal", credentialsJson.get("client_id").getAsString());
            SecurityToken securityToken = SecurityToken.fromJson(securityTokenJson.toString());
            this.securityTokenContainer.setSecurityToken(securityToken);

            System.out.println("*****************************************************************");
            System.out.println("(SecurityTokenContainer): " + this.securityTokenContainer);
            System.out.println("(SecurityToken): " + this.securityTokenContainer.getSecurityToken());
            System.out.println("(Principal): " + this.securityTokenContainer.getSecurityToken().getPrincipal());
            System.out.println("*****************************************************************");
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @Query("documentByLakeId")
    @Description("Get Document by DataLakeId")
    public List<Document> documentByLakeId(@Name("dataLakeId") String dataLakeId) {
        this.setSecurityToken();

        return this.queryService.getDocumentByLakeId(dataLakeId);
    }

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

    @Query("personByName")
    @Description("Get a Person By Name")
    public List<Person> personByName(@Name("name") String personName) {
        return personService.getPersonByName(personName);
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
