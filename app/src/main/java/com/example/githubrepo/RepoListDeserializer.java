package com.example.githubrepo;

import com.example.githubrepo.models.Repository;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sarah_neo on 20/02/2017.
 */

public class RepoListDeserializer implements JsonDeserializer<List<Repository>> {
    private static final String ITEMS = "items";
    private static final String OWNER = "owner";
    private static final String AVATAR_URL = "avatar_url";
    private static final String TOTAL_COUNT = "total_count";

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

            JsonObject ownerObj = repoObj.get(OWNER).getAsJsonObject();
            String avatarUrl = ownerObj.get(AVATAR_URL).getAsString();

            repo.setOwnerAvatarUrl(avatarUrl);
            repo.setTotal(total);

            repoList.add(repo);
        }

        return repoList;
    }
}
