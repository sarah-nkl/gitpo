package com.example.githubrepo;

import com.example.githubrepo.models.Owner;
import com.example.githubrepo.models.Repository;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by sarah_neo on 20/02/2017.
 */

public class RepoListDeserializer implements JsonDeserializer<List<Repository>> {
    private static final String ITEMS = "items";
    private static final String OWNER = "owner";

    @Override
    public List<Repository> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                                                            throws JsonParseException {
        List<Repository> repoList = new ArrayList<>();
        JsonObject rootObj = json.getAsJsonObject();

        JsonArray repoArray = rootObj.getAsJsonArray(ITEMS);

        for (JsonElement element : repoArray) {
            Repository repo = context.deserialize(element, Repository.class);
            JsonObject repoObj = element.getAsJsonObject();

            JsonElement ownerElement = repoObj.get(OWNER);
            Owner owner = context.deserialize(ownerElement, Owner.class);

            repo.setOwner(owner);

            repoList.add(repo);
        }

        return repoList;
    }
}
