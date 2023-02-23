## Multithreaded web crawler for images

### Problem Statement
The goal of this task is to perform a web crawl on a URL string provided by the user. From the crawl, we need to parse out all of the images on that web page and return a JSON array of strings that represent the URLs of all images on the page. [Jsoup](https://jsoup.org/) is a great basic library for crawling and is already included as a maven dependency in this project.

### Functionality Implemented
- A web crawler that can find all images on the web page(s) that it crawls
- Can crawl sub-pages to find more images
- Multi-threading has been done to improve performance and allow crawling of multiple sub-pages at the same time
- Crawl has been restricted to the same domain as the input URL
- This is a friendly crawler that avoids re-crawling any pages that have already been visited
- Ability to categorize images into GIFs, logos/icons, and just general images

## Structure of the project
The ImageFinder servlet is found in `src/main/java/com/eulerity/hackathon/imagefinder/ImageFinder.java`

The main landing page for this project can be found in `src/main/webapp/index.html`. This page serves as the starting page for the web application

Finally, the root directory of this project contains the `pom.xml`. This contains the project configuration details used by maven to build the project

## Running the Project

### Requirements
Before beginning, make sure you have the following installed and ready to use
- Maven 3.5 or higher
- Java 8

### Setup
To start, open a terminal window and navigate to wherever you unzipped to the root directory `imagefinder`. To build the project, run the command:

>`mvn package`

If all goes well you should see some lines that end with "BUILD SUCCESS". When you build your project, maven should build it in the `target` directory. To clear this, you may run the command:

>`mvn clean`

To run the project, use the following command to start the server:

>`mvn clean test package jetty:run`

You should see a line at the bottom that says "Started Jetty Server". Now, if you enter `localhost:8080` into your browser, you should see the `index.html` welcome page!

### Testing
- I have added two JUnits to test the fail and success cases
- I have also provided a test-links.txt that lists down most of the sites I used to test the crawler