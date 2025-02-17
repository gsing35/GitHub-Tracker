
import java.io.*;
import java.lang.reflect.Array;
import java.util.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.json.simple.JSONArray;

public class Main {
    public static void main(String[] args) {
        Scanner Scan = new Scanner(System.in);
        String username;
        System.out.println("Please enter username of person you want to see.");

        username = Scan.nextLine();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = null;
        try {
            URI uri = new URI("https://api.github.com/users/" + username + "/repos");
            fetchAPIResponse(uri, client, response);
        } catch(Exception e) {
            System.out.println("Error: "); 
            e.printStackTrace();
        }
    }

    private static void fetchAPIResponse(URI uri, HttpClient client, HttpResponse<String> response) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(Duration.ofMinutes(1))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch(Exception e) {
            System.out.println("Error: "); 
            e.printStackTrace();
        }

        if(response.statusCode() != 200) {
            System.out.println("Error: status code " + response.statusCode()); 
            return;
        }
        
        String JSONResponse = parseResponse(response);

        //Parse the JSON response
        JSONParser parser = new JSONParser();
        JSONArray JSONarray = null;
        try {
            JSONarray = (JSONArray) parser.parse(JSONResponse);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        JSONObject object = null;
        String date = "";
        for (int i = 0; i < JSONarray.size(); i++) {
            JSONObject repo = (JSONObject) JSONarray.get(i);
            String pushedAt = (String) repo.get("pushed_at");
            
            if(date.isEmpty() || pushedAt.compareTo(date) > 0) {
                date = pushedAt;
                object = repo;
            }
        }
        if(object != null) {
            //System.out.println("Most recent repo is: " + object.get("name") + " pushed at: " + date + "coded in: " + object.get("language")));
            printJSON(object, 0);
        }
      

    }

    private static String parseResponse(HttpResponse<String> response) {
        StringBuilder JSONResponse = new StringBuilder();
        Scanner Scanner = new Scanner(response.body());
        while(Scanner.hasNextLine()) {
            JSONResponse.append(Scanner.nextLine());
        }
        Scanner.close();
        return JSONResponse.toString();
    }

    private static void printIndent(int indent) {
        for(int i = 0; i < indent; i++) {
            System.out.printf("   ");
        }
    }

    private static void printJSON(JSONObject object, int indent) {
        System.out.printf("{\n");

        for(Object keyObject : object.keySet()) {
            String key = (String) keyObject;
            Object val =  object.get(key);

            printIndent(indent + 1);
            System.out.printf("%s: ", key);
            if(val instanceof JSONObject) {
                printJSON((JSONObject) val, indent + 1);
            }
            else {
                System.out.printf("%s\n: ", val);
            }
        }
        printIndent(indent);
        System.out.printf("}\n");

        }
    }


