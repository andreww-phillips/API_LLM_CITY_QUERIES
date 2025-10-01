package com.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import java.io.IOException;
import java.util.Scanner;

/*
Little project I did to get a better grasp of the relationship between APIs and LLms.
In this project I'll be using gemini-2.5-flash. It starts by prompting the user for a city name,
where it'll send an HTTP POST request to the LLM to check if it's a valid name. The user will then
get prompted for a query choice (Cities current temp, time or population). Depending on this
choice it'll send an HTTP POST request accordingly to the LLM. It'll lastly display the LLMs response.
 */

public class Main
{
    //Read API key from environment
    private static final String API_KEY = System.getenv("GEMINI_API_KEY");
    //LLM API Endpoint
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

    //Checks if input choice for Query is one of the given integers
    public static boolean validChoice(int choice)
    {
        boolean valid = false;
        if(choice == 1 || choice == 2 || choice == 3){
            valid = true;
        }
        return valid;
    }

    //Uses LLM to check if the given city name is valid
    public static boolean validCity(String city, OkHttpClient client, ObjectMapper mapper) throws JsonProcessingException {
        boolean result = false;
        String verificationPrompt = "Is " + city + " an existing city? Answer with *only* 'YES' or 'NO'. Do not include any other text, punctuation, or quotation marks.";

        //Create the text prompt
        JsonNode textPart = mapper.createObjectNode()
                .put("text", verificationPrompt);

        JsonNode partArray = mapper.createArrayNode()
                .add(textPart);

        //Build LLM Prompt
        JsonNode contentsNode = mapper.createObjectNode()
                .put("role", "user")
                .set("parts", partArray);

        //Build an array to store the prompt
        JsonNode contentsArray = mapper.createArrayNode().add(contentsNode);

        //Create payload (holds API requests/responses)
        JsonNode payload = mapper.createObjectNode()
                .put("model", "gemini-2.5-flash")
                .set("contents", contentsArray);

        //Convert payload into a string to send as a HTTP request
        String jsonPayload = mapper.writeValueAsString(payload);

        //Determine content type of HTTP request body
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        //Structure the HTTP request body
        RequestBody body = RequestBody.create(jsonPayload, JSON);

        //HTTP POST request
        Request request = new Request.Builder()
                .url(API_URL)
                .header("x-goog-api-key", API_KEY)
                .post(body)
                .build();

        //Send HTTP POST request with execption handling
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()){
                return result;
            }
            

            System.out.println("HTTP Status Code: " + response.code());
            System.out.println("LLM RAW JSON Response: " + responseBody);

            JsonNode jsonResponse = mapper.readTree(responseBody);

            String llmAnswer = jsonResponse.get("candidates")
                    .get(0)
                    .get("content")
                    .get("parts")
                    .get(0)
                    .get("text")
                    .asText()
                    .trim();

            //Capture LLM response
            String cleanAnswer = llmAnswer.replaceAll("[^a-zA-Z0-9]", "");
            result = cleanAnswer.equalsIgnoreCase("YES");
        }catch (IOException io){
            System.out.println(io.getMessage());
        }
        //Return result
        return result;
    }

    public static void main( String[] args ) throws IOException {

        //Checks if API_KEY has been input
        if(API_KEY == null || API_KEY.isBlank()){
            System.err.println("ERROR: API KEY not set");
            System.err.println("Please set the GEMINI_API_KEY by using: export GEMINI_API_KEY=\"YOUR_GEMINI_API_KEY\"");
            return;
        }

        //Create Objects for OkHttpClient and ObjectMapper and Scanner
        OkHttpClient client = new OkHttpClient();
        ObjectMapper mapper = new ObjectMapper();
        Scanner kb = new Scanner(System.in);

        String cityName;

        //Implement validCity() for validity of cities name
        do {
            System.out.print("Enter a city's name: ");
            cityName = kb.nextLine().trim();
            // Check if city exists using LLM
            if (!validCity(cityName, client, mapper)) {
                System.err.println("ERROR: City does not exist according to LLM.");
                continue;
            }
            break;

        } while (true);

        int choice = 0;
        String finalPrompt = " ";

        //Continuously prompts user for a query choice until user inputs a valid integer
        while(!validChoice(choice)) {
            System.out.print("\n===== List of Query's ====\n");
            System.out.println("    1. What is the current weather in " + cityName + " ?");
            System.out.println("    2. What is the current time in " + cityName + " ?");
            System.out.println("    3. What is the current population in " + cityName + " ?");
            System.out.print("Enter your choice (1-3): ");

            //Check if user enters an integer
            if(kb.hasNextInt()) {
                choice = kb.nextInt();
                kb.nextLine();
            }else{
                System.err.println("ERROR: Invalid input type! Must be a number between 1 and 3! Try again.");
                kb.next();
                choice = 0;
                continue; //Skips rest of loop, re-displaying the table of query's
            }

            //Set finalPrompt variable according to users choice
            switch (choice) {
                case 1:
                    finalPrompt = "What is the current weather in " + cityName + " ?";
                    break;
                case 2:
                    finalPrompt = "What is the current time in " + cityName + " ?";
                    break;
                case 3:
                    finalPrompt = "What is the current population in " + cityName + " ?";
                    break;
                default:
                    System.err.println("ERROR: Invalid choice! Try again.");
                    break;
            }
        }
        //Build text prompt
        JsonNode textPart = mapper.createObjectNode()
                .put("text", finalPrompt);

        //Build an array to store the prompt
        JsonNode partArray = mapper.createArrayNode()
                .add(textPart);

        //Build LLM Prompt
        JsonNode messageNode = mapper.createObjectNode()
            .put("role","user")
            .set("parts",partArray);

        //Build an array to store the prompt
        JsonNode messageArray = mapper.createArrayNode().add(messageNode);

        //Create payload (holds API requests/responses)
        JsonNode payload = mapper.createObjectNode()
                .put("model", "gemini-2.5-flash")
                .set("contents", messageArray);

        //Convert payload to a string to send as a HTTP request
        String jsonPayload = mapper.writeValueAsString(payload);

        //Determine content type of HTTP request body
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        //Structure the HTTP request body
        RequestBody body = RequestBody.create(jsonPayload, JSON);

        //HTTP POST request
        Request request = new Request.Builder()
                .url(API_URL)
                //Only allows a post request if user is already authorized using their OPEN_AI_API_KEY
                .header("x-goog-api-key", API_KEY)
                //Sends the HTTP request body built earlier
                .post(body)
                .build();

        //Set exception handling for the post request
        try(Response response = client.newCall(request).execute()){
            if(!response.isSuccessful()){
                System.err.println("Error: " + response);
                return;
            }

            //Read the body of the LLMs output as  a String
            String responseBody = response.body().string();

            //Parse response JSON
            JsonNode jsonResponse = mapper.readTree(responseBody);

            //Extract the generated text by the LLM
            String output = jsonResponse
                    .get("candidates")
                    .get(0)
                    .get("content")
                    .get("parts")
                    .get(0)
                    .get("text")
                    .asText();

            //Print LLM response
            System.out.println("LLM Response: " + output);
        }catch(IOException io){
            System.err.println(io.getMessage());
        }
    }
}
