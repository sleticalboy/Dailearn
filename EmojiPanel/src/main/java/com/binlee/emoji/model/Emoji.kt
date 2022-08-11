package com.binlee.emoji.model

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable
import java.util.Objects

/**
 * Created on 19-7-16.
 *
 * @author leebin
 */
class Emoji : Serializable, Cloneable {
  //表情唯一标识
  var uuid: String? = null

  @JSONField(serialize = false, deserialize = false)
  var packUuid: String? = null
  var name: String? = null
  var size = 0
  var height = 0
  var width = 0
  var sha1: String? = null
  var path: String? = null

  // uri or url
  var thumbnail: String? = null

  @JSONField(name = "updated_at")
  var updatedAt: String? = null

  @JSONField(name = "description")
  var description: Desc? = null

  @JSONField(name = "sort_order")
  var sortOrder = 0
  var key: String? = null
  var replaceKey: String? = null

  /**
   * 使用时间，最近使用表情查询此字段，降序
   */
  @JSONField(serialize = false, deserialize = false)
  var lastUseTime = -1L

  @JSONField(serialize = false, deserialize = false)
  var isDelete = false
    get() = field || "del_btn" == key

  /**
   * 小表情对应的本地资源 id
   */
  @JSONField(serialize = false, deserialize = false)
  var resId = -1

  // 小表情是打包在 apk 中的资源文件
  @get:JSONField(serialize = false, deserialize = false)
  val isSmall: Boolean
    get() = key != null || replaceKey != null

  @get:JSONField(serialize = false, deserialize = false)
  val isAdd: Boolean
    get() = "add_btn" == key

  public override fun clone(): Emoji {
    try {
      super.clone()
    } catch (ignored: CloneNotSupportedException) {
    }
    // deep cloning
    return JSON.parseObject(JSON.toJSONString(this), Emoji::class.java)
  }

  // 最近使用表情利用 HashSet 去重，所以重写 equals 和 hashCode 方法
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || javaClass != other.javaClass) return false
    val emoji = other as Emoji
    return uuid == emoji.uuid && sha1 == emoji.sha1
  }

  override fun hashCode(): Int {
    return Objects.hash(uuid, sha1)
  }

  class Desc : Serializable {

    var cn: String? = null
    var en: String? = null

    companion object {
      private const val serialVersionUID = -5377411869795035352L
    }
  }

  companion object {
    private const val serialVersionUID = -45683397753071549L
  }
}