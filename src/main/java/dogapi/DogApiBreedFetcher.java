package dogapi;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;
import okhttp3.ResponseBody;

/**
 * BreedFetcher implementation that relies on the dog.ceo API.
 * Note that all failures get reported as BreedNotFoundException
 * exceptions to align with the requirements of the BreedFetcher interface.
 */
public class DogApiBreedFetcher implements BreedFetcher {
    private final OkHttpClient client = new OkHttpClient();

    /**
     * Fetch the list of sub breeds for the given breed from the dog.ceo API.
     * @param breed the breed to fetch sub breeds for
     * @return list of sub breeds for the given breed
     * @throws BreedNotFoundException if the breed does not exist (or if the API call fails for any reason)
     */
    @Override
    public List<String> getSubBreeds(String breed) throws BreedNotFoundException {
        String url = "https://dog.ceo/api/breed/" + breed.toLowerCase() + "/list";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {

            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new BreedNotFoundException(breed);
            }

            String bodyString = responseBody.string();
            JSONObject json = new JSONObject(bodyString);

            String status = json.optString("status", "error");

            if (!"success".equalsIgnoreCase(status)) {
                // Covers "error" and any weird/unexpected response
                throw new BreedNotFoundException(breed);
            }

            JSONArray subBreedArray = json.getJSONArray("message");
            List<String> result = new ArrayList<>();
            for (int i = 0; i < subBreedArray.length(); i++) {
                result.add(subBreedArray.getString(i));
            }

            return result;

        } catch (IOException e) {
            // Network/IO issues are also treated as "breed not found"
            throw new BreedNotFoundException(breed);
        }
    }
}