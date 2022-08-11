package com.binlee.emoji.model

import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable
import java.util.Objects

/**
 * Created on 19-7-16.
 *
 * @author leebin
 */
class EmojiGroup : Serializable {

  //表情和表情包的关联字段(唯一)
  var uuid: String? = null
  var name: String? = null
  var description: String? = null
  var size = 0
  var sha1: String? = null
  var path: String? = null
  var thumbnail: String? = null
  var surface: String? = null
  var advertisement: String? = null

  @JSONField(name = "updated_at")
  var updatedAt: String? = null
  var count = 0
  var type = 0

  /**
   * 切换到其他组时，当前组出于第几页
   */
  var lastChildIndex = 0

  /**
   * 退出重进时直接进入此分组，入库时 0 或 1
   */
  var isLastGroup = false

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || javaClass != other.javaClass) return false
    val that = other as EmojiGroup
    return uuid == that.uuid && sha1 == that.sha1
  }

  override fun hashCode(): Int {
    return Objects.hash(uuid, sha1)
  }

  companion object {
    private const val serialVersionUID = 2836324578616418084L
  }
}