package ufrn.br.TRFNotifica.util;

import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

public class ProcessoJsonBuilderUtil {
    public static String getJsonString(String numeroProcesso) throws JSONException {
        JSONObject match = new JSONObject();
        JSONObject query = new JSONObject();
        JSONObject jsonObj = new JSONObject();
        String jsonString;

        match.put("numeroProcesso", numeroProcesso);
        query.put("match", match);
        jsonObj.put("query", query);
        jsonString = jsonObj.toString();

        return jsonString;
    }

    public static String getJsonString(Integer classeCodigo, Integer orgaoJulgadorCodigo, Integer size) throws JSONException {
        JSONObject finalObject = buildCommonJson(classeCodigo, orgaoJulgadorCodigo, size);
        return finalObject.toString();
    }

    public static String getJsonString(Integer classeCodigo, Integer orgaoJulgadorCodigo, Integer size, Long sortValue) throws JSONException {
        JSONObject finalObject = buildCommonJson(classeCodigo, orgaoJulgadorCodigo, size);
        //finalObject.getJSONArray("search_after").put(sortValue);
        JSONArray searchAfterArray = new JSONArray();
        searchAfterArray.put(sortValue);
        finalObject.put("search_after", searchAfterArray);
        return finalObject.toString();
    }

    private static JSONObject buildCommonJson(Integer classeCodigo, Integer orgaoJulgadorCodigo, Integer size) throws JSONException {
        JSONObject queryObject = new JSONObject();
        JSONObject boolObject = new JSONObject();
        JSONArray mustArray = new JSONArray();
        JSONObject matchClasse = new JSONObject();
        JSONObject matchOrgaoJulgador = new JSONObject();
        JSONObject timestampObject = new JSONObject();
        JSONArray sortArray = new JSONArray();
        JSONObject finalObject = new JSONObject();
        String jsonString;

        matchClasse.put("match", new JSONObject().put("classe.codigo", classeCodigo));
        mustArray.put(matchClasse);
        matchOrgaoJulgador.put("match", new JSONObject().put("orgaoJulgador.codigo", orgaoJulgadorCodigo));
        mustArray.put(matchOrgaoJulgador);
        boolObject.put("must", mustArray);
        queryObject.put("bool", boolObject);
        timestampObject.put("@timestamp", new JSONObject().put("order", "asc"));
        sortArray.put(timestampObject);
        finalObject.put("size", size);
        finalObject.put("query", queryObject);
        finalObject.put("sort", sortArray);
        //jsonString = finalObject.toString();

        return finalObject;
    }

    /*
    // Primeira página
    public static String getJsonString(Integer classeCodigo, Integer orgaoJulgadorCodigo, Integer size) throws JSONException {
        JSONObject queryObject = new JSONObject();
        JSONObject boolObject = new JSONObject();
        JSONArray mustArray = new JSONArray();
        JSONObject matchClasse = new JSONObject();
        JSONObject matchOrgaoJulgador = new JSONObject();
        JSONObject timestampObject = new JSONObject();
        JSONArray sortArray = new JSONArray();
        JSONObject finalObject = new JSONObject();
        String jsonString;

        matchClasse.put("match", new JSONObject().put("classe.codigo", classeCodigo));
        mustArray.put(matchClasse);
        matchOrgaoJulgador.put("match", new JSONObject().put("orgaoJulgador.codigo", orgaoJulgadorCodigo));
        mustArray.put(matchOrgaoJulgador);
        boolObject.put("must", mustArray);
        queryObject.put("bool", boolObject);
        timestampObject.put("@timestamp", new JSONObject().put("order", "asc"));
        sortArray.put(timestampObject);
        finalObject.put("size", size);
        finalObject.put("query", queryObject);
        finalObject.put("sort", sortArray);
        jsonString = finalObject.toString();

        return jsonString;
    }

    // Segunda página em diante
    public static String getJsonString(Integer classeCodigo, Integer orgaoJulgadorCodigo, Integer size, Long sortValue) throws JSONException {
        JSONObject queryObject = new JSONObject();
        JSONObject boolObject = new JSONObject();
        JSONArray mustArray = new JSONArray();
        JSONObject matchClasse = new JSONObject();
        JSONObject matchOrgaoJulgador = new JSONObject();
        JSONObject timestampObject = new JSONObject();
        JSONArray sortArray = new JSONArray();
        JSONArray searchAfterArray = new JSONArray();
        JSONObject finalObject = new JSONObject();
        String jsonString;

        matchClasse.put("match", new JSONObject().put("classe.codigo", classeCodigo));
        mustArray.put(matchClasse);
        matchOrgaoJulgador.put("match", new JSONObject().put("orgaoJulgador.codigo", orgaoJulgadorCodigo));
        mustArray.put(matchOrgaoJulgador);
        boolObject.put("must", mustArray);
        queryObject.put("bool", boolObject);
        timestampObject.put("@timestamp", new JSONObject().put("order", "asc"));
        sortArray.put(timestampObject);
        searchAfterArray.put(sortValue);
        finalObject.put("size", size);
        finalObject.put("query", queryObject);
        finalObject.put("sort", sortArray);
        finalObject.put("search_after", searchAfterArray);
        jsonString = finalObject.toString();

        return jsonString;
    }
    */
}
