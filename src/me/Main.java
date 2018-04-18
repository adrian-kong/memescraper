package me;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * quick reddit meme scraper
 *
 * @author Adrian
 * @since 17/04/2018
 */
public class Main {
    public static String after = "";

    public static void main(String[] args) throws Exception {
        int pages = Integer.parseInt(args[0]);
        /**
         * connect to page
         */
        for (int i = 0; i < pages; i++) {
            new Thread() {
                @Override
                public synchronized void start() {
                    try {
                        URL url = new URL("https://www.reddit.com/r/memes/.json?limit=100&count=" + (pages * 100) + (!after.isEmpty() ? "&after=" + after : ""));
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                        connection.setRequestMethod("GET");
                        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.13) Gecko/20080311 Firefox/2.0.0.13");
                        connection.setRequestProperty("Accept", "application/json");
                        connection.setRequestProperty("Content-Type", "application/json");
                        InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
                        BufferedReader reader = new BufferedReader(inputStreamReader);

                        String line;
                        StringBuilder data = new StringBuilder();
                        while ((line = reader.readLine()) != null)

                        {
                            data.append(line);
                        }
                        inputStreamReader.close();

                        long time = System.currentTimeMillis();

                        /**
                         * Key is title
                         * Value is url
                         */
                        Map<String, String> memeData = new HashMap();

                        /**
                         * parse page using org.json lib
                         * poorly coded
                         */
                        JSONObject parse = new JSONObject(data.toString()).getJSONObject("data");
                        Main.after = parse.getString("after");
                        JSONArray obj = parse.getJSONArray("children");
                        for (int i = 0; i < obj.length(); i++) {
                            JSONObject postData = obj.getJSONObject(i).getJSONObject("data");
                            if (!postData.has("preview"))
                                continue;
                            JSONArray imageData = postData.getJSONObject("preview").getJSONArray("images");
                            /**
                             * only keep alphabet and digits or else we can't save the file because no special characters allowed
                             */
                            String title = postData.getString("title").replaceAll("[^a-zA-Z0-9 ]+", "");
                            System.out.println(title);
                            for (int j = 0; j < imageData.length(); j++) {
                                JSONObject jObject = imageData.getJSONObject(j);
                                String memeURL;
                                /**
                                 * check is gif
                                 */
                                if (!jObject.getJSONObject("variants").isNull("gif")) {
                                    memeURL = jObject.getJSONObject("variants").getJSONObject("gif").getJSONObject("source").getString("url");
                                    title += ".gif";
                                } else {
                                    memeURL = jObject.getJSONObject("source").getString("url");
                                    title += ".jpg";
                                }

                                memeData.put(title, memeURL);

                                System.out.println("Found " + title + " {" + memeURL + "}");
                            }
                        }
                        System.out.println("Parse to JSON (Time taken: " + (System.currentTimeMillis() - time) + "ms)");

                        /**
                         * make a folder called memes
                         */
                        File savePath = new File("memes");
                        savePath.mkdir();

                        /**
                         * save the memes from url to %title%.jpg
                         */
                        for (String img : memeData.keySet()) {
                            File imgPath = new File(savePath, img);
                            if (!imgPath.exists())
                                try (InputStream in = new URL(memeData.get(img)).openStream()) {
                                    Files.copy(in, imgPath.toPath());
                                    System.out.println("Saving " + img);
                                }
                            else
                                System.out.println(img + " already exists!");
                        }

                        System.out.println("Downloaded " + memeData.size() + " posts (Time taken: " + (System.currentTimeMillis() - time) + "ms)");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }.start();
        }
    }
}
