package org.example.utils;
import java.util.ArrayList;
import java.util.List;
public class BrazilStates {
    private static final String[][] STATES = {
        {"AC", "Acre"},
        {"AL", "Alagoas"},
        {"AP", "Amapá"},
        {"AM", "Amazonas"},
        {"BA", "Bahia"},
        {"CE", "Ceará"},
        {"DF", "Distrito Federal"},
        {"ES", "Espírito Santo"},
        {"GO", "Goiás"},
        {"MA", "Maranhão"},
        {"MT", "Mato Grosso"},
        {"MS", "Mato Grosso do Sul"},
        {"MG", "Minas Gerais"},
        {"PA", "Pará"},
        {"PB", "Paraíba"},
        {"PR", "Paraná"},
        {"PE", "Pernambuco"},
        {"PI", "Piauí"},
        {"RJ", "Rio de Janeiro"},
        {"RN", "Rio Grande do Norte"},
        {"RS", "Rio Grande do Sul"},
        {"RO", "Rondônia"},
        {"RR", "Roraima"},
        {"SC", "Santa Catarina"},
        {"SP", "São Paulo"},
        {"SE", "Sergipe"},
        {"TO", "Tocantins"}
    };
    public static List<StateInfo> getAllStates() {
        List<StateInfo> states = new ArrayList<>();
        for (String[] state : STATES) {
            states.add(new StateInfo(state[0], state[1]));
        }
        return states;
    }
    public static List<String> getStateCodes() {
        List<String> codes = new ArrayList<>();
        for (String[] state : STATES) {
            codes.add(state[0]);
        }
        return codes;
    }
    public static String getStateName(String code) {
        if (code == null) {
            return null;
        }
        String upperCode = code.toUpperCase();
        for (String[] state : STATES) {
            if (state[0].equals(upperCode)) {
                return state[1];
            }
        }
        return null;
    }
    public static boolean isValidStateCode(String code) {
        return getStateName(code) != null;
    }
    public static class StateInfo {
        private String code;
        private String name;
        public StateInfo(String code, String name) {
            this.code = code;
            this.name = name;
        }
        public String getCode() {
            return code;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }
        public void setCode(String code) {
            this.code = code;
        }
    }
}