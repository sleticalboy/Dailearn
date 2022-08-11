package com.binlee.emoji.model

import java.io.Serializable

/**
 * Created on 19-7-30.
 *
 * @author leebin
 */
class WrappedEmoji : Serializable {

  var mEmoji: Emoji? = null
  var mGroup: EmojiGroup? = null
  var mEmojiList: List<Emoji>? = null

  companion object {
    private const val serialVersionUID = 3392703176362269326L
  }
}