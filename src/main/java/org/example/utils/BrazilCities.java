package org.example.utils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class BrazilCities {
    private static final Map<String, List<CityInfo>> CITIES_BY_STATE = new HashMap<>();
    static {
        List<CityInfo> spCities = new ArrayList<>();
        spCities.add(new CityInfo("São Paulo", "SP"));
        spCities.add(new CityInfo("Campinas", "SP"));
        spCities.add(new CityInfo("Guarulhos", "SP"));
        spCities.add(new CityInfo("São Bernardo do Campo", "SP"));
        spCities.add(new CityInfo("Santo André", "SP"));
        spCities.add(new CityInfo("Osasco", "SP"));
        CITIES_BY_STATE.put("SP", spCities);
        List<CityInfo> rjCities = new ArrayList<>();
        rjCities.add(new CityInfo("Rio de Janeiro", "RJ"));
        rjCities.add(new CityInfo("Niterói", "RJ"));
        rjCities.add(new CityInfo("Campos dos Goytacazes", "RJ"));
        rjCities.add(new CityInfo("Duque de Caxias", "RJ"));
        CITIES_BY_STATE.put("RJ", rjCities);
        List<CityInfo> mgCities = new ArrayList<>();
        mgCities.add(new CityInfo("Belo Horizonte", "MG"));
        mgCities.add(new CityInfo("Uberlândia", "MG"));
        mgCities.add(new CityInfo("Contagem", "MG"));
        mgCities.add(new CityInfo("Juiz de Fora", "MG"));
        CITIES_BY_STATE.put("MG", mgCities);
        List<CityInfo> prCities = new ArrayList<>();
        prCities.add(new CityInfo("Curitiba", "PR"));
        prCities.add(new CityInfo("Londrina", "PR"));
        prCities.add(new CityInfo("Maringá", "PR"));
        prCities.add(new CityInfo("Ponta Grossa", "PR"));
        CITIES_BY_STATE.put("PR", prCities);
    }
    public static List<CityInfo> getCitiesByState(String stateCode) {
        if (stateCode == null) {
            return new ArrayList<>();
        }
        String upperCode = stateCode.toUpperCase();
        List<CityInfo> cities = CITIES_BY_STATE.get(upperCode);
        if (cities == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(cities);
    }
    public static List<String> getCityNamesByState(String stateCode) {
        List<CityInfo> cities = getCitiesByState(stateCode);
        List<String> names = new ArrayList<>();
        for (CityInfo city : cities) {
            names.add(city.getName());
        }
        return names;
    }
    public static boolean isValidCity(String cityName, String stateCode) {
        if (cityName == null || stateCode == null) {
            return false;
        }
        List<CityInfo> cities = getCitiesByState(stateCode);
        for (CityInfo city : cities) {
            if (city.getName().equalsIgnoreCase(cityName)) {
                return true;
            }
        }
        return false;
    }
    public static class CityInfo {
        private String name;
        private String stateCode;
        public CityInfo(String name, String stateCode) {
            this.name = name;
            this.stateCode = stateCode;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getStateCode() {
            return stateCode;
        }
        public void setStateCode(String stateCode) {
            this.stateCode = stateCode;
        }
    }
}