package com.eulerity.hackathon.imagefinder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(
        name = "ImageFinder",
        urlPatterns = {"/main"}
)


public class ImageFinder extends HttpServlet {

    private String domainHost = "";
    private HashSet<String> visitedUrls = new HashSet<>();
    private HashSet<String> imageUrls = new HashSet<>();
    private HashSet<String> iconUrls = new HashSet<>();
    private HashSet<String> gifUrls = new HashSet<>();
    public static HashMap<String, String[]> resultMap = new HashMap<>();
    private String description;

    private ExecutorService executorService = null;
    private static final long serialVersionUID = 1L;

    protected static final Gson GSON = new GsonBuilder().create();
    private final Logger logger = Logger.getLogger(ImageFinder.class.getName());

    @Override
    protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        //if executor service was running previously, shut it down
        if (executorService != null) {
            executorService.shutdownNow();
        }
        executorService = Executors.newFixedThreadPool(10);
        resp.setContentType("text/json");
        String path = req.getServletPath();
        String url = req.getParameter("url");
        System.out.println("Got request of:" + path + " with query param:" + url);
        URL domain = new URL(url);

        //Empty/reset the variables
        domainHost = domain.getHost();
        visitedUrls = new HashSet<>();
        imageUrls = new HashSet<>();
        gifUrls = new HashSet<>();
        iconUrls = new HashSet<>();
        resultMap = new HashMap<>();
        description = "";
        crawl(url);

        try {
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            logger.log(Level.FINE, e.getMessage());
        }

        //populating the result map with arrays of gifs, icons, and images
        //this makes it easier to reference these lists through their keys while displaying it in the frontend
        String[] imageArr = imageUrls.toArray(new String[imageUrls.size()]);
        String[] iconArr = iconUrls.toArray(new String[iconUrls.size()]);
        String[] gifArr = gifUrls.toArray(new String[gifUrls.size()]);
        String[] descriptionArr = new String[1];
        descriptionArr[0] = description;

        resultMap.put("Icons", iconArr);
        resultMap.put("GIFs", gifArr);
        resultMap.put("Images", imageArr);
        resultMap.put("Description", descriptionArr);

        resp.getWriter().print(GSON.toJson(resultMap));
    }


    private int crawl(String url) {
        //adding thread sleep to keep the crawler friendly and avoid sending too many requests to the site
        try {
            Thread.sleep((long) (Math.random() * 100L));
        } catch (InterruptedException e) {
            logger.log(Level.FINE, "Thread sleep interrupted: " + e.getMessage());
        }

        if (!visitedUrls.contains(url) && isSameDomain(url, domainHost)) {
            visitedUrls.add(url);

            //executor service allows async execution of threads
            executorService.submit(() -> {
                try {
                    //using Jsoup to extract elements from the webpage
                    Document doc = Jsoup.connect(url).get();
                    try {
                        description = doc.select("meta[name=description]").get(0)
                                .attr("content");
                    } catch (Exception e) {
                        logger.log(Level.FINE, "Error in extracting description: " + e.getMessage());
                    }

                    Elements imageTags = doc.select("img[src]");
                    Elements linkTags = doc.select("link[rel=\"icon\"]").select("href");

                    for (Element image : imageTags) {
                        String imageUrl = image.absUrl("src");

                        //classifying images based on their file extension
                        if (imageUrl.substring(imageUrl.lastIndexOf('.') + 1).equalsIgnoreCase("gif")) {
                            gifUrls.add(imageUrl);
                        } else if (imageUrl.substring(imageUrl.lastIndexOf('.') + 1).equalsIgnoreCase("ico")
                                || imageUrl.substring(imageUrl.lastIndexOf('/') + 1).contains("logo")
                                || imageUrl.substring(imageUrl.lastIndexOf('/') + 1).contains("icon")) { //if the image name has "icon" or "logo" classify it as an icon
                            iconUrls.add(imageUrl);
                        } else {
                            imageUrls.add(imageUrl);
                        }

                    }

                    for (Element link : linkTags) {
                        String linkUrl = link.absUrl("href");
                        //Assuming that all file formats in linkTag with rel attribute as icon will be logos
                        iconUrls.add(linkUrl);
                    }

                    Elements links = doc.select("a[href]");
                    for (Element link : links) {
                        String nextUrl = link.absUrl("href");
                        //avoiding unnecessary function call by checking if webpage has already been visited
                        if (!visitedUrls.contains(nextUrl) && isSameDomain(nextUrl, domainHost)) crawl(nextUrl);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    logger.log(Level.FINE, e.getMessage());
                }
            });
        }
        return 0;
    }

    //checking if current URL's domain matches parent URL's host domain
    private boolean isSameDomain(String url, String mainHost) {
        try {
            String urlHost = new URL(url).getHost();
            return urlHost.equalsIgnoreCase(mainHost);
        } catch (Exception e) {
            logger.log(Level.FINE, e.getMessage());
            return false;
        }

    }
}
