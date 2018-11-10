package com.example.githubrepo;

import com.example.githubrepo.models.Repository;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by sarah_neo on 20/02/2017.
 */

public class RepoListDeserializer implements JsonDeserializer<List<Repository>> {
    private static final String ITEMS = "items";
    private static final String OWNER = "owner";
    private static final String AVATAR_URL = "avatar_url";
    private static final String TOTAL_COUNT = "total_count";
    private static final String PUSHED_AT = "pushed_at";

    @Override
    public List<Repository> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                                                            throws JsonParseException {
        List<Repository> repoList = new ArrayList<>();
        JsonObject rootObj = json.getAsJsonObject();

        int total = rootObj.get(TOTAL_COUNT).getAsInt();

        JsonArray repoArray = rootObj.getAsJsonArray(ITEMS);

        for (JsonElement element : repoArray) {
            Repository repo = context.deserialize(element, Repository.class);
            JsonObject repoObj = element.getAsJsonObject();

            // Get pushed at
            try {
                String pushedAt = repoObj.get(PUSHED_AT).getAsString();
                repo.setPushedAt(convertStringToTimeMillis(pushedAt));
            } catch (NullPointerException e) {

            } catch (ParseException e) {

            } catch (UnsupportedOperationException e) {

            }


            // Get owner
            JsonObject ownerObj = repoObj.get(OWNER).getAsJsonObject();
            String avatarUrl = ownerObj.get(AVATAR_URL).getAsString();

            repo.setOwnerAvatarUrl(avatarUrl);
            repo.setTotal(total);

            repoList.add(repo);
        }

        return repoList;
    }

    private static long convertStringToTimeMillis(String dateString) throws ParseException {
        long milliseconds = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        try {
            Date d = sdf.parse(dateString.replaceAll("Z$", "+0000"));
            milliseconds = d.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return milliseconds;
    }
}
