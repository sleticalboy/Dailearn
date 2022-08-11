package com.binlee.emoji.model

import android.content.Context
import com.binlee.emoji.helper.EmojiHelper
import com.binlee.emoji.helper.LogHelper
import com.binlee.emoji.helper.OsHelper
import com.binlee.emoji.helper.ThreadHelper
import java.util.*
import kotlin.math.ceil

/**
 * Created on 19-7-21.
 *
 *
 * 表情数据仓库，所有与表情相关的数据操作均与此类进行交互，不直接操作数据库
 *
 * @author leebin
 */
class EmojiRepo private constructor() {

  companion object {
    private const val TAG = "EmojiRepo"

    /**
     * 内存中缓存的表情数据，如果内存被回收，则重新从数据库读取
     */
    private var sEmojiMap: MutableMap<EmojiGroup, Array<Array<Emoji?>>> = LinkedHashMap()

    /**
     * 内存中记录的最近使用表情，当数据更新到数据库之后将会清空
     */
    private var sRecordedUsedEmojis: MutableSet<Emoji?> = HashSet()

    /**
     * 是否是静态初始化
     */
    private var sInit = false

    /**
     * 数据源发生变化时更新 ui
     */
    private val sCallbacks: MutableList<OnDataChangeCallback> = ArrayList()

    fun staticInit() {
      // 为确保 EmojiRepo 类中的静态代码块执行
      // 注：单纯的加载一个类并不能使其静态代码块得以执行，需要调用一个静态的字段或者方法
    }

    private fun preloadData() {
      if (!OsHelper.isMasterProcess) {
        return
      }
      sEmojiMap.clear()
      // 1, 查询所有分组
      // 2, 遍历分组查询组内所有表情
      // 3, 遍历组内表情并填充 sEmojiMap
      // 首次升级新版表情初始化
      if (!EmojiHelper.hasGroup(EmojiHelper.smallGroupUuid)) {
        EmojiHelper.instance.initDefaultEmoji(OsHelper.app())
      }
      // 同步 server 数据
      EmojiHelper.instance.initEmojiGroupList(OsHelper.app())
      // 1, 查询所有分组
      val groupList = EmojiHelper.instance.getCurrentEmojiGroupList(OsHelper.app())
      if (groupList.isEmpty()) {
        return
      }
      for (key in groupList) {
        val pageData = getPageData(OsHelper.app(), key, sInit)
          ?: continue
        // 填充 sEmojiMap
        emojiMap()[key] = pageData
      }
    }

    // 不直接对外暴露
    private fun emojiMap(): MutableMap<EmojiGroup, Array<Array<Emoji?>>> {
      // if (sEmojiMap.isEmpty()) {
      //     preloadData()
      // }
      return sEmojiMap
    }

    private fun recordedEmojis(): MutableSet<Emoji?> {
      return sRecordedUsedEmojis
    }

    /**
     * 当表情数据发生变动时，执行更新逻辑
     * 1，新增或移除表情包（增量）；未实现
     *
     *
     * 2，新增或移除自定义表情（增量）；已实现
     * 3，自定义表情排序发生变化（增量）；已实现
     *
     * @return true 更新, false 没有更新
     */
    fun updateEmojiMap(group: EmojiGroup?, preload: Boolean): Boolean {
      requireNotNull(group) { "EmojiGroup is null." }
      val pageData = getPageData(OsHelper.app(), group, preload)
      if (pageData != null) {
        emojiMap()[group] = pageData
        return true
      }
      return false
    }

    /**
     * 生成表情分页数据
     * @param preload 是否需要预加载数据
     */
    private fun getPageData(
      context: Context?, key: EmojiGroup?,
      preload: Boolean
    ): Array<Array<Emoji?>>? {
      // 2, 遍历分组查询组内所有表情
      val emojis = EmojiHelper.instance.getEmojiList(context, key)
      if (emojis == null || emojis.isEmpty()) {
        return null
      }
      val pageData: Array<Array<Emoji?>>
      // 3, 遍历组内表情填充 sEmojiMap
      if (EmojiHelper.isSmallGroup(key)) {
        // 小表情每页3行7列, 每页的最后一个元素为`删除按钮`
        val pageCount = ceil(emojis.size / 20f.toDouble()).toInt()
        // 填充最近使用表情
        val recentEmojis = EmojiHelper.instance.recentlyUsedEmojis
        if (recentEmojis?.isEmpty()!!) {
          pageData = arrayOf(arrayOfNulls(pageCount))
        } else {
          // 第一页数据要给最近使用表情留出位置
          pageData = arrayOf(arrayOfNulls(pageCount + 1))
          pageData[0] = arrayOfNulls(recentEmojis.size)
          for (i in recentEmojis.indices) {
            val emoji = recentEmojis[i]
            emoji.resId = EmojiHelper.getDrawableId(context, emoji.key)
            pageData[0][i] = emoji
          }
        }
        val startIndex = if (pageData.size == pageCount) 0 else 1
        for (i in startIndex until pageData.size) {
          val start = (if (startIndex == 1) i - 1 else i) * 20
          val end = if (start + 20 > emojis.size) emojis.size else start + 20
          // 最后一个是`删除按钮`
          pageData[i] = arrayOfNulls(end - start + 1)
          var j = start
          var index = 0
          while (j < end) {
            val emoji = emojis[j]
            if (preload) {
              EmojiHelper.mapRes(context, emoji)
            }
            pageData[i][index] = emoji
            // 填充删除按钮
            if (index == end - start - 1) {
              pageData[i][index + 1] = EmojiHelper.createDelEmoji(context)
            }
            j++
            index++
          }
        }
      } else {
        // 其他分组表情
        val pageCount = ceil(emojis.size / 8f.toDouble()).toInt()
        pageData = arrayOf(arrayOfNulls(pageCount))
        for (i in pageData.indices) {
          val start = i * 8
          val end = if (start + 8 > emojis.size) emojis.size else start + 8
          // 无需删除按钮
          pageData[i] = arrayOfNulls(end - start)
          var j = start
          var index = 0
          while (j < end) {
            val emoji = emojis[j]
            if (preload) {
              EmojiHelper.mapRes(context, emoji)
            }
            pageData[i][index] = emoji
            j++
            index++
          }
        }
      }
      return pageData
    }

    /**
     * 最近使用表情按使用时间倒序排列
     */
    private fun sortRecentEmojis(emojis: List<Emoji?>) {
      // l < r -> -1
      // l == r -> 0
      // l > r -> 1
      Collections.sort(emojis) { left: Emoji?, right: Emoji? ->
        if (left != null && right != null) {
          return@sort right.lastUseTime.compareTo(left.lastUseTime)
        } else {
          return@sort 0
        }
      }
    }

    /**
     * 将内存中记录的值与原有的值合并(丢弃旧值，保留新值)
     *
     * @param list     新值
     * @param oldValue 旧值
     */
    private fun mergeOldValues(list: MutableList<Emoji?>, oldValue: Array<Emoji?>) {
      for (emoji in oldValue) {
        if (!list.contains(emoji)) {
          list.add(emoji)
        }
      }
    }

    /**
     * 更新最近使用表情
     *
     * @return 若有更新，返回更新后的数据；否则返回 null
     */
    fun updateRecentEmojis(): Array<Array<Emoji?>>? {
      if (recordedEmojis().isEmpty()) {
        return null
      }
      val cachedEmojis = recordedEmojis().toMutableList()
      for (key in emojiMap().keys) {
        if (EmojiHelper.isSmallGroup(key)) {
          val oldValue = emojiMap().remove(key)
          val newValue: Array<Array<Emoji?>>
          if (oldValue!![0].size <= 9 && oldValue[0].isNotEmpty()) {
            // 已有最近使用表情
            newValue = arrayOf(arrayOfNulls(oldValue.size))
            // copy 原数据
            System.arraycopy(oldValue, 1, newValue, 1, oldValue.size - 1)
            // 添加旧数据并去重
            mergeOldValues(cachedEmojis, oldValue[0])
          } else {
            // 第一次有最近使用表情
            newValue = arrayOf(arrayOfNulls(oldValue.size + 1))
            // copy 原数据
            System.arraycopy(oldValue, 0, newValue, 1, oldValue.size)
          }
          // 降序排列排序
          sortRecentEmojis(cachedEmojis)
          // 新增数据
          val length = if (cachedEmojis.size >= 9) 9 else cachedEmojis.size
          newValue[0] = arrayOfNulls(length)
          for (i in 0 until length) {
            newValue[0][i] = cachedEmojis[i]
          }
          // 缓存新数据
          emojiMap()[key] = newValue
          // 清空内存
          recordedEmojis().clear()
          // 更新数据库
          ThreadHelper.runOnWorker {
            // 最多更新 9 条数据
            EmojiHelper.updateRecentlyUsedEmojis(cachedEmojis.subList(0, length))
          }
          return newValue
        }
      }
      return null
    }

    /**
     * 记录最近使用表情
     */
    fun recordEmoji(emoji: Emoji) {
      // 新的要把旧的替换掉
      recordedEmojis().remove(emoji)
      recordedEmojis().add(emoji)
    }

    fun updateLastUsedGroup(groups: List<EmojiGroup?>?) {
      // 更新内存
      for (key in emojiMap().keys) {
        for (group in groups!!) {
          if (key == group) {
            key.isLastGroup = group.isLastGroup
            key.lastChildIndex = group.lastChildIndex
          }
        }
      }
      // 更新数据库
      val uid: Int = EmojiHelper.userId
      if (uid < 0) {
        return
      }
      ThreadHelper.runOnWorker { DbManager.get().updateLastUsedEmojiGroup(groups, uid) }
    }

    val emojiGroups: MutableList<EmojiGroup>
      get() = emojiMap().keys.toMutableList()

    fun getGroupData(group: EmojiGroup?): Array<Array<Emoji?>>? {
      return emojiMap()[group]
    }

    fun registerCallback(callback: OnDataChangeCallback) {
      if (!sCallbacks.contains(callback)) {
        sCallbacks.add(callback)
      }
    }

    fun unregisterCallback(callback: OnDataChangeCallback?) {
      sCallbacks.remove(callback)
    }

    /**
     * 添加或者移除表情包时回调
     *
     * @param group 表情分组, 当此值为 null 时，视为自定义表情发生变化, isAdd 参数不再处理
     * @param isAdd 新增 or 移除
     */
    fun notifyDataChanged(group: EmojiGroup?, isAdd: Boolean) {
      // 先处理处理数据再回调？
      // 需要先更新内存中的数据，再回调
      // ThreadHelper.runOnWorker(() -> {
      //     // 更新数据
      //     // ...
      //     if (sCallbacks.size() == 0) {
      //         return;
      //     }
      //     ThreadHelper.runOnMain(() -> {
      //         // 刷新 ui
      //     });
      // });
      if (sCallbacks.size == 0) {
        return
      }
      for (callback in sCallbacks) {
        if (group == null || EmojiHelper.isCustomGroup(group)) {
          callback.onCustomEmojiChanged()
        } else {
          callback.onGroupChanged(group, isAdd)
        }
      }
    }

    init {
      // 找个合适的时机预加载这个类以便提前初始化数据从而提升用户体验
      // 如果是首次安装，此方法执行大概在 380 ms 左右，所以预加载还是很有必要的
      // 非首次安装在 35 ms 左右
      val start: Long = LogHelper.logTime()
      sInit = true
      preloadData()
      sInit = false
      // 接着预加载图片资源？
      LogHelper.debug(TAG, "static setupClient: " + LogHelper.elapsedMillis(start) + "ms")
    }
  }

  interface OnDataChangeCallback {
    /**
     * 添加或者移除表情包时回调
     *
     * @param group 表情分组
     * @param isAdd 新增 or 移除
     */
    fun onGroupChanged(group: EmojiGroup, isAdd: Boolean)

    /**
     * 当自定义表情发生变化时回调
     */
    fun onCustomEmojiChanged()
  }

  init {
    throw AssertionError("no instance")
  }
}