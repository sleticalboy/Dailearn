package com.binlee.emoji.model

import android.util.Log

class DbManager private constructor() {

  companion object {
    fun get(): DbManager = Singleton.manager
    private const val TAG = "DbManager"
  }

  fun updateLastUsedEmojiGroup(groups: List<EmojiGroup?>?, uid: Int) {
    Log.d(TAG, "updateLastUsedEmojiGroup() called with: groups = $groups, uid = $uid")
  }

  fun queryRecentUsedEmojis(uid: Int): List<Emoji>? {
    Log.d(TAG, "queryRecentUsedEmojis() called with: uid = $uid")
    return null
  }

  fun updateRecentUsedEmojis(emojis: List<Emoji?>?, uid: Int) {
    Log.d(TAG, "updateRecentUsedEmojis() called with: emojis = $emojis, uid = $uid")
  }

  fun queryEmojiListByGroupUuid(uuid: String?, uid: Int): List<Emoji>? {
    Log.d(TAG, "queryEmojiListByGroupUuid() called with: uuid = $uuid, uid = $uid")
    return null
  }

  fun queryCustomEmojis(uid: Int): List<Emoji>? {
    Log.d(TAG, "queryCustomEmojis() called with: uid = $uid")
    return null
  }

  fun hasEmojiGroup(uuid: String?): Boolean {
    Log.d(TAG, "hasEmojiGroup() called with: uuid = $uuid")
    return false
  }

  fun hasCustomEmoji(sha1: String?): Boolean {
    Log.d(TAG, "hasCustomEmoji() called with: sha1 = $sha1")
    return false
  }

  fun getCustomEmojiGroup(userId: Int): EmojiGroup? {
    Log.d(TAG, "getCustomEmojiGroup() called with: userId = $userId")
    return null
  }

  fun queryEmojiGroupByType(type: Int, userId: Int): EmojiGroup? {
    Log.d(TAG, "queryEmojiGroupByType() called with: type = $type, userId = $userId")
    return null
  }

  fun insertEmojiGroup(group: EmojiGroup?, userId: Int) {
    Log.d(TAG, "insertEmojiGroup() called with: group = $group, userId = $userId")
  }

  fun insertEmojiList(list: List<Emoji>?, userId: Int) {
    Log.d(TAG, "insertEmojiList() called with: list = $list, userId = $userId")
  }

  fun queryAllEmojiGroup(userId: Int): List<EmojiGroup>? {
    Log.d(TAG, "queryAllEmojiGroup() called with: userId = $userId")
    return null
  }

  fun deleteEmojisByGroup(uuid: String?) {
    Log.d(TAG, "deleteEmojisByGroup() called with: uuid = $uuid")
  }

  fun updateEmojiGroupNameThumbnailCount(group: EmojiGroup?, userId: Int) {
    Log.d(TAG, "updateEmojiGroupNameThumbnailCount() called with: group = $group, userId = $userId")
  }

  fun queryEmojiGroupByUuid(uuid: String?, userId: Int): EmojiGroup? {
    Log.d(TAG, "queryEmojiGroupByUuid() called with: uuid = $uuid, userId = $userId")
    return null
  }

  fun deleteEmojiList(userId: Int, list: List<Emoji>?) {
    Log.d(TAG, "deleteEmojiList() called with: userId = $userId, list = $list")
  }

  private object Singleton {
    val manager = DbManager()
  }
}