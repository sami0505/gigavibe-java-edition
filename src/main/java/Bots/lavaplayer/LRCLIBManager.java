package Bots.lavaplayer;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class LRCLIBManager {
    public static String getLyrics(AudioTrack track) {
        if (track.getInfo().title == null || track.getInfo().title.equalsIgnoreCase("unknown title")) {
            return "";
        }
        String url = createURL(track);
        if (url.isEmpty()) {
            return "";
        }

        try {
            URL requestURL = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) requestURL.openConnection();
            connection.setRequestMethod("GET");

            StringBuilder responseBuilder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }
            String response = responseBuilder.toString();
            if (response.equals("[]")) {
                return "";
            }

            String lyrics = parseLyrics(response);
            if (lyrics == null || lyrics.equalsIgnoreCase("null")) {
                return "";
            }
            return lyrics;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private static String createURL(AudioTrack track) {
        StringBuilder urlBuilder = new StringBuilder();
        String duration = "";
        urlBuilder.append("https://lrclib.net/api/search?q=");

        if (!(track.getInfo().length > 432000000) || !(track.getInfo().length <= 1)) {
            duration = "&duration=" + (int) (track.getInfo().length / 1000);
        }

        String title = track.getInfo().title;
        if (track.getInfo().isStream && Objects.equals(track.getSourceManager().getSourceName(), "http")) {
            duration = "";
            title = RadioDataFetcher.getStreamSongNow(track.getInfo().uri);
        }

        urlBuilder.append(java.net.URLEncoder.encode(title, StandardCharsets.UTF_8));
        String url = urlBuilder.toString();
        if (url.contains("+%28")) {
            url = url.split("\\+%28")[0].trim();
        }
        if (url.contains("%5B")) {
            url = url.split("\\+%5B")[0].trim();
        }
        if (url.toLowerCase().contains("+ft.")) {
            url = url.split("\\+ft\\.")[0].trim();
        }
        url = url + duration;
        return url;
    }

    private static String parseLyrics(String rawJson) {
        Object object = JSONValue.parse(rawJson);
        JSONObject trackDetailsObject = null;
        try {
            trackDetailsObject = (JSONObject) ((JSONArray) object).get(1);
        } catch (Exception ignored) {
            System.err.println("No lyrics were found for this track.");
        }
        if (trackDetailsObject == null) {
            return "";
        }
        return (String) trackDetailsObject.get("plainLyrics");
    }
}
