# API_LLM_CITY_QUERIES
Using the relationship between APIs and LLMs, I have developed a software that uses the relationship between API HTTP requests and LLMs to generate information about cities. As of right now I have limited the prompts to a list of 3 queries; weather, time and population.

## SETTING YOUR GEMINI_API_KEY
Initially visit this site to generate a Gemini API Key: https://aistudio.google.com/app/apikey <br>
(Save the API Key in a secure location)

Then set your API Key using the following command line:
Mac: export GEMINI_API_KEY="YOUR_API_KEY"
Windows: set GEMINI_API_KEY="YOUR_API_KEY"

## COMPILING AND RUNNING YOUR MAVEN PROJECT
Use the following command line on both Mac and Windows:
mvn clean compile exec:java -Dexec.mainClass="com.example.Main"

1. mvn clean -> Deletes target directory for a fresh start
2. compile -> Compiles projects source code
3. exec:java -> Exec Maven Plugin that runs a Java class directly from the compiled project
4. -Dexec.mainClass="com.example.Main" -> Determines which class to execute (Main) 
