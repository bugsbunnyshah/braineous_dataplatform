package prototype.graphql;

import java.util.Collection;

public class AnimalTO {

    private String id;
    private String name;
    private String color;
    private Collection<String> countryIds;
    private Collection<CountryTO> countries;

    public AnimalTO() {
    }

    public AnimalTO(String id, String name, String color, Collection<String> countryIds, Collection<CountryTO> countries) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.countryIds = countryIds;
        this.countries = countries;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }
    public void setColor(String color) {
        this.color = color;
    }

    public Collection<String> getCountryIds() {
        return countryIds;
    }
    public void setCountryIds(Collection<String> countryIds) {
        this.countryIds = countryIds;
    }

    public Collection<CountryTO> getCountries() {
        return countries;
    }
    public void setCountries(Collection<CountryTO> countries) {
        this.countries = countries;
    }

}