package json;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * An object representing an item in Escape from Tarkov.
 */
public class Item {

    /**
     * The name of the item.
     */
    private String myName;

    /**
     * The shortened name of the item.
     */
    private String myShortName;

    /**
     * The name of the vendor who pays the most for this item.
     */
    private String myVendorName;

    /**
     * The lowest 24h flea market price of the item.
     */
    private int myFleaPrice;

    /**
     * The max vendor price of the item.
     */
    private int myVendorPrice;

    /**
     * The difference between the flea price and vendor price.
     */
    private int myPriceDifference;

    /**
     * Maps a data field to an Item object.
     */
    private static Function<Object, Item> MAP_FUNCTION = o -> {
        JSONObject j = (JSONObject) o;
        JSONArray sf =  j.getJSONArray("sellFor");
        int vendPrice = getMaxVendorPrice(sf);
        String vendName = getVendorNameFromPrice(sf, vendPrice);
        return new Item(j.getString("name"), j.getString("shortName"),
                vendName, getFleaPrice(j), vendPrice);
    };

    /**
     * Creates an item with a name, shortened name, vendor name and price, and 24h low flea price.
     * @param theName the name of the item.
     * @param theShortName the shortened name of the item.
     * @param theVendorName the vendor's name who pays the most for the item.
     * @param theFleaPrice the 24h low flea market price of the item.
     * @param theVendorPrice the vendor price of the item.
     */
    private Item(final String theName, final String theShortName, final String theVendorName,
                final int theFleaPrice, final int theVendorPrice) {
        myName = theName;
        myShortName = theShortName;
        myVendorName = theVendorName;
        myFleaPrice = theFleaPrice;
        myVendorPrice = theVendorPrice;
        myPriceDifference = myVendorPrice - myFleaPrice;
    }

    /**
     * Creates an array of Items sorted by price difference descending and stripped of banned from flea items.
     * @return An array of Items.
     * @throws IOException No.
     * @throws InterruptedException Clue.
     */
    public static Item[] getItems() throws IOException, InterruptedException {
        String jsonString = getItemsJSON();
        JSONObject jsonObject = new JSONObject(jsonString);
        JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("items");
        List<Item> list = new ArrayList<>();
        for (Object o : jsonArray) {
            list.add(MAP_FUNCTION.apply(o));
        }
        Collections.sort(list, (a,b) -> b.myPriceDifference - a.myPriceDifference);
        list.removeIf(f -> f.myFleaPrice == 0);
        return list.toArray(Item[]::new);
    }

    /**
     * Gets the highest nonflea vendor price from the sellFor field.
     * @param theSellFor the sellFor JSONObject.
     * @return The max vendor price.
     */
    private static int getMaxVendorPrice(JSONArray theSellFor) {
        int max = 0;
        for (Object sf : theSellFor) {
            JSONObject j = (JSONObject) sf;
            int cur = j.getInt("priceRUB");
            String vendor = j.getJSONObject("vendor").getString("name");
            if (!vendor.equals("Flea Market") && cur > max) {
                max = cur;
            }
        }
        return max;
    }

    /**
     * From the sellFor field gets the vendor name using the vendor price.
     * @param theSellFor
     * @param thePrice
     * @return the vendor name.
     */
    private static String getVendorNameFromPrice (JSONArray theSellFor, int thePrice) {
        for (Object sf : theSellFor) {
            JSONObject j = (JSONObject) sf;
            if (j.getInt("priceRUB") == thePrice) {
                return j.getJSONObject("vendor").getString("name");
            }
        }
        return "";
    }

    /**
     * A helper function to get 24h low price without errors.
     * @param theJSONObject The data JSON object.
     * @return 24h low price, or maybe 0.
     */
    private static int getFleaPrice(JSONObject theJSONObject) {
        try{
            return theJSONObject.getInt("low24hPrice");
        } catch(Exception e) {
            return 0;
        }
    }

    /**
     * Returns a JSON String of various Tarkov related items, their name, vendor, prices.
     * @return a JSON String of various Tarkov items.
     * @throws IOException No idea.
     * @throws InterruptedException What causes these.
     */
    private static String getItemsJSON() throws IOException, InterruptedException {
        // Largely copypasted from https://tarkov.dev/api/#java-11
        HttpClient client = HttpClient.newBuilder().build();
        String query = "{\"query\": \"{ items {name shortName low24hPrice sellFor{vendor{name} priceRUB}} }\"}";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.tarkov.dev/graphql"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(query))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    /**
     * Returns the name of the item.
     * @return the name of the item.
     */
    public String getName() {
        return myName;
    }

    /**
     * Returns the price difference of the item.
     * @return the price difference of the item.
     */
    public int getPriceDifference() {
        return myPriceDifference;
    }

    /**
     * Returns the vendor price of the item.
     * @return the vendor price of the item.
     */
    public int getVendorPrice(){
        return myVendorPrice;
    }

    @Override
    public String toString() {
        return  myName  + " :   " + myPriceDifference + " (" + myVendorName + ")";
    }
}
