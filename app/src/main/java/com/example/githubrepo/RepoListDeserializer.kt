package com.example.githubrepo

import com.example.githubrepo.models.Repository
import com.example.githubrepo.models.RepositoryList
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class RepoListDeserializer : JsonDeserializer<RepositoryList> {
    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): RepositoryList {
        val list = mutableListOf<Repository>()
        val rootObj = json.asJsonObject
        val total = rootObj[TOTAL_COUNT].asInt
        val repoArray = rootObj.getAsJsonArray(ITEMS)
        for (element in repoArray) {
            val repo = context.deserialize<Repository>(element, Repository::class.java)
            val repoObj = element.asJsonObject
            val ownerObj = repoObj[OWNER].asJsonObject
            val avatarUrl = ownerObj[AVATAR_URL].asString
            repo.ownerAvatarUrl = avatarUrl
            repo.total = total
            list.add(repo)
        }

        return RepositoryList(list, total)
    }

    companion object {
        private const val ITEMS = "items"
        private const val OWNER = "owner"
        private const val AVATAR_URL = "avatar_url"
        private const val TOTAL_COUNT = "total_count"
    }
}