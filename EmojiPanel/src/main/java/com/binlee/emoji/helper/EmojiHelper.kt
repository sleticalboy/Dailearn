package com.binlee.emoji.helper

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ImageSpan
import android.util.Base64
import android.util.Log
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.binlee.emoji.ImageAdapter
import com.binlee.emoji.R
import com.binlee.emoji.model.DbManager
import com.binlee.emoji.model.Emoji
import com.binlee.emoji.model.Emoji.Desc
import com.binlee.emoji.model.EmojiGroup
import com.binlee.emoji.span.CustomAtSpan
import com.binlee.emoji.span.EmojiSpan
import okio.buffer
import okio.source
import java.io.ByteArrayOutputStream
import java.util.*
import java.util.regex.Pattern
import kotlin.math.roundToInt

class EmojiHelper private constructor() {

  companion object {
    const val MX_EMOJI_GROUP_DEFAULT_UNIQUE_UUID = "mx_emoji_group_default_uuid"
    const val MX_EMOJI_DEFAULT_UNIQUE_UUID = "mx_emoji_default_uuid"
    private const val MX_EMOJI_DEFAULT_DRAWABLE_PREFIX = "em_"
    const val MX_EMOJI_GROUP_DEFAULT_TYPE = 0x100 //默认小表情
    const val MX_EMOJI_GROUP_TYPE_CUSTOM = 1 //自定义表情
    const val MX_EMOJI_GROUP_TYPE_DEFAULT = 3 //预制表情包
    const val MX_EMOJI_GROUP_TYPE_STORE = 2 //商店表情包
    private var MAX_EMOJI_SIZE = 0
    private const val REGEX =
      "/::\\)|/::~|/::B|/::\\||/:8-\\)|/::lt|/::\\$|/::X|/::Z|/::\\.\\(|/::-\\||/::@|/::P|/::D|/::O|/::\\(|/::\\+|/:--b|/::Q|/::T|/:,@P|/:,@-D|/::d|/:,@o|/::hg|/:\\|-\\)|/::!|/::L|/::gt|/::,@|/:,@f|/::-S|/:\\?|/:,@x|/:,@@|/::8|/:,@!|/:!!!|/:xx|/:bye|/:wipe|/:dig|/:handclap|/:and-\\(|/:B-\\)|/:lt@|/:@gt|/::-O|/:gt-\\||/:P-\\(|/::\\.\\||/:X-\\)|/::\\*|/:@x|/:8\\*|/:pd|/:ltWgt|/:beer|/:basketb|/:oo|/:coffee|/:eat|/:pig|/:rose|/:fade|/:showlove|/:heart|/:break|/:cake|/:li|/:bome|/:kn|/:footb|/:ladybug|/:shit|/:moon|/:sun|/:gift|/:hug|/:strong|/:weak|/:share|/:v|/:@\\)|/:jj|/:@@|/:bad|/:lvu|/:no|/:ok|/:em_1:/|/:em_2:/|/:em_3:/|/:em_4:/|/:em_5:/|/:em_6:/|/:em_7:/|/:em_8:/|/:em_9:/|/:em_10:/|/:em_11:/|/:em_12:/|/:em_13:/|/:em_14:/|/:em_15:/|/:em_16:/|/:em_17:/|/:em_18:/|/:em_19:/|/:em_20:/|/:em_21:/|/:em_22:/|/:em_23:/|/:em_24:/|/:em_25:/|/:em_26:/|/:em_27:/|/:em_28:/|/:em_29:/|/:em_30:/|/:em_31:/|/:em_32:/|/:em_33:/|/:em_34:/|/:em_35:/|/:em_36:/|/:em_37:/|/:em_38:/|/:em_39:/|/:em_40:/|/:em_41:/|/:em_42:/|/:em_43:/|/:em_44:/|/:em_45:/|/:em_46:/|/:em_47:/|/:em_48:/|/:em_49:/|/:em_50:/|/:em_51:/|/:em_52:/|/:em_53:/|/:em_54:/|/:em_55:/|/:em_56:/|/:em_57:/|/:em_58:/|/:em_59:/|/:em_60:/|/:em_61:/|/:em_62:/|/:em_63:/|/:em_64:/|/:em_65:/|/:em_66:/|/:em_67:/|/:em_68:/|/:em_69:/|/:em_70:/|/:em_71:/|/:em_72:/|/:em_73:/|/:em_74:/|/:em_75:/|/:em_76:/|/:em_77:/|/:em_78:/|/:em_79:/|/:em_80:/|/:em_81:/|/:em_82:/|/:em_83:/|/:em_84:/|/:em_85:/|/:em_86:/|/:em_87:/|/:em_88:/|/:em_89:/|/:em_90:/|/:em_91:/|/:em_92:/|/:em_93:/|/:em_94:/|/:em_95:/|/:em_96:/|/:em_97:/|/:em_98:/|/:em_99:/|/:em_100:/|/:em_101:/|/:em_102:/|/:em_103:/|/:em_104:/|/:em_105:/|/:em_106:/|/:em_107:/|/:em_108:/|/:em_109:/|/:em_110:/|/:em_111:/|/:em_112:/|/:em_113:/|/:em_114:/|/:em_115:/|/:em_116:/|/:em_117:/|/:em_118:/|/:em_119:/|/:em_120:/|/:em_121:/|/:em_122:/|/:em_123:/|/:em_124:/|/:em_125:/|/:em_126:/|/:em_127:/|/:em_128:/|/:em_129:/|/:em_130:/|/:em_131:/|/:em_132:/|/:em_133:/|/:em_134:/|/:em_135:/|/:em_136:/|/:em_137:/|/:em_138:/|/:em_139:/|/:em_140:/|/:em_141:/|/:em_142:/|/:em_143:/|/:em_144:/|/:em_145:/|/:em_146:/|/:em_147:/|/:em_148:/|/:em_149:/|/:em_150:/|/:em_151:/|/:em_152:/|/:em_153:/|/:em_154:/|/:em_155:/|/:em_156:/|/:em_157:/|/:em_158:/|/:em_159:/|/:em_160:/"
    private val PATTERN = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE)

    private val SMILEY_KEYS = arrayOf(
      "/:em_1:/",
      "/:em_2:/",
      "/:em_3:/",
      "/:em_4:/",
      "/:em_5:/",
      "/:em_6:/",
      "/:em_7:/",
      "/:em_8:/",
      "/:em_9:/",
      "/:em_10:/",
      "/:em_11:/",
      "/:em_12:/",
      "/:em_13:/",
      "/:em_14:/",
      "/:em_15:/",
      "/:em_16:/",
      "/:em_17:/",
      "/:em_18:/",
      "/:em_19:/",
      "/:em_20:/",
      "/:em_21:/",
      "/:em_22:/",
      "/:em_23:/",
      "/:em_24:/",
      "/:em_25:/",
      "/:em_26:/",
      "/:em_27:/",
      "/:em_28:/",
      "/:em_29:/",
      "/:em_30:/",
      "/:em_31:/",
      "/:em_32:/",
      "/:em_33:/",
      "/:em_34:/",
      "/:em_35:/",
      "/:em_36:/",
      "/:em_37:/",
      "/:em_38:/",
      "/:em_39:/",
      "/:em_40:/",
      "/:em_41:/",
      "/:em_42:/",
      "/:em_43:/",
      "/:em_44:/",
      "/:em_45:/",
      "/:em_46:/",
      "/:em_47:/",
      "/:em_48:/",
      "/:em_49:/",
      "/:em_50:/",
      "/:em_51:/",
      "/:em_52:/",
      "/:em_53:/",
      "/:em_54:/",
      "/:em_55:/",
      "/:em_56:/",
      "/:em_57:/",
      "/:em_58:/",
      "/:em_59:/",
      "/:em_60:/",
      "/:em_61:/",
      "/:em_62:/",
      "/:em_63:/",
      "/:em_64:/",
      "/:em_65:/",
      "/:em_66:/",
      "/:em_67:/",
      "/:em_68:/",
      "/:em_69:/",
      "/:em_70:/",
      "/:em_71:/",
      "/:em_72:/",
      "/:em_73:/",
      "/:em_74:/",
      "/:em_75:/",
      "/:em_76:/",
      "/:em_77:/",
      "/:em_78:/",
      "/:em_79:/",
      "/:em_80:/",
      "/:em_81:/",
      "/:em_82:/",
      "/:em_83:/",
      "/:em_84:/",
      "/:em_85:/",
      "/:em_86:/",
      "/:em_87:/",
      "/:em_88:/",
      "/:em_89:/",
      "/:em_90:/",
      "/:em_91:/",
      "/:em_92:/",
      "/:em_93:/",
      "/:em_94:/",
      "/:em_95:/",
      "/:em_96:/",
      "/:em_97:/",
      "/:em_98:/",
      "/:em_99:/",
      "/:em_100:/",
      "/:em_101:/",
      "/:em_102:/",
      "/:em_103:/",
      "/:em_104:/",
      "/:em_105:/",
      "/:em_106:/",
      "/:em_107:/",
      "/:em_108:/",
      "/:em_109:/",
      "/:em_110:/",
      "/:em_111:/",
      "/:em_112:/",
      "/:em_113:/",
      "/:em_114:/",
      "/:em_115:/",
      "/:em_116:/",
      "/:em_117:/",
      "/:em_118:/",
      "/:em_119:/",
      "/:em_120:/",
      "/:em_121:/",
      "/:em_122:/",
      "/:em_123:/",
      "/:em_124:/",
      "/:em_125:/",
      "/:em_126:/",
      "/:em_127:/",
      "/:em_128:/",
      "/:em_129:/",
      "/:em_130:/",
      "/:em_131:/",
      "/:em_132:/",
      "/:em_133:/",
      "/:em_134:/",
      "/:em_135:/",
      "/:em_136:/",
      "/:em_137:/",
      "/:em_138:/",
      "/:em_139:/",
      "/:em_140:/",
      "/:em_141:/",
      "/:em_142:/",
      "/:em_143:/",
      "/:em_144:/",
      "/:em_145:/",
      "/:em_146:/",
      "/:em_147:/",
      "/:em_148:/",
      "/:em_149:/",
      "/:em_150:/",
      "/:em_151:/",
      "/:em_152:/",
      "/:em_153:/",
      "/:em_154:/",
      "/:em_155:/",
      "/:em_156:/",
      "/:em_157:/",
      "/:em_158:/",
      "/:em_159:/",
      "/:em_160:/"
    )
    private val SMILEY_TEXTS = arrayOf(
      "[哈哈大笑]",
      "[笑哭]",
      "[亲亲哟]",
      "[一脸嫌弃]",
      "[眨眼]",
      "[哭了]",
      "[开心]",
      "[挤眉毛]",
      "[哦耶]",
      "[捂脸]",
      "[滑稽]",
      "[机智]",
      "[嘿嘿哟]",
      "[邪恶]",
      "[不开心]",
      "[可爱]",
      "[笑容]",
      "[色眯眯]",
      "[眼冒金星]",
      "[纠结]",
      "[流汗了]",
      "[呵呵]",
      "[尬]",
      "[舔嘴嘴]",
      "[眯眯眼]",
      "[思考一下]",
      "[气气的]",
      "[好怕哟]",
      "[别说话]",
      "[哇]",
      "[困困的]",
      "[要崩溃了]",
      "[沮丧流汗]",
      "[害羞了]",
      "[我都睡了]",
      "[翻白眼]",
      "[唉唉]",
      "[调皮]",
      "[酷酷的]",
      "[惊讶]",
      "[流口水]",
      "[见钱眼开]",
      "[哇哦]",
      "[哭唧唧]",
      "[有点难过]",
      "[轻微难过]",
      "[巨难过]",
      "[伤心的很]",
      "[难受]",
      "[难过了]",
      "[愤怒了]",
      "[懵了]",
      "[出洋相]",
      "[难过流汗]",
      "[带口罩]",
      "[伤心欲绝]",
      "[发烧]",
      "[头炸了]",
      "[感冒]",
      "[天使]",
      "[恶魔]",
      "[青蛙]",
      "[彩虹小马]",
      "[蜗牛]",
      "[小幽灵]",
      "[外星人]",
      "[兔兔]",
      "[捂眼小猴]",
      "[捂耳小猴]",
      "[偷笑小猴]",
      "[猪猪]",
      "[便便]",
      "[奶牛]",
      "[旺财]",
      "[笑脸猫咪]",
      "[色猫咪]",
      "[震惊猫咪]",
      "[狐狸]",
      "[老虎]",
      "[狮子]",
      "[否定之男]",
      "[否定之女]",
      "[ok之男]",
      "[ok之女]",
      "[举手之男]",
      "[举手之女]",
      "[兔女郎]",
      "[捂脸男人]",
      "[捂脸女人]",
      "[僵尸美女]",
      "[僵尸帅哥]",
      "[无奈男人]",
      "[无奈女人]",
      "[兔基友]",
      "[一家四口]",
      "[一家三口]",
      "[男婴]",
      "[女婴]",
      "[女人]",
      "[男人]",
      "[双手赞成]",
      "[鼓掌]",
      "[握手]",
      "[真好]",
      "[不喜欢]",
      "[拳头]",
      "[拼搏]",
      "[左勾拳]",
      "[右勾拳]",
      "[十]",
      "[剪刀手]",
      "[LOVEU]",
      "[上面]",
      "[下面]",
      "[二头肌]",
      "[瓦肯人你好]",
      "[左边]",
      "[右边]",
      "[拜托]",
      "[好呀]",
      "[女侦探]",
      "[女医生]",
      "[女博士]",
      "[男博士]",
      "[女领导]",
      "[男领导]",
      "[女科学家]",
      "[男科学家]",
      "[女王]",
      "[国王]",
      "[女圣诞老人]",
      "[圣诞老人]",
      "[警察]",
      "[工人]",
      "[警卫]",
      "[女人鱼]",
      "[男人鱼]",
      "[女吸血鬼]",
      "[男吸血鬼]",
      "[天使娃娃]",
      "[黑色月亮]",
      "[月亮脸]",
      "[月亮]",
      "[星星]",
      "[群星]",
      "[晴天]",
      "[四叶草]",
      "[多云]",
      "[阴天]",
      "[下雨]",
      "[雷阵雨]",
      "[雪]",
      "[水珠]",
      "[雨伞]",
      "[破壳]",
      "[小鸡]",
      "[雪人]",
      "[火]",
      "[闪电]",
      "[圣诞树]"
    )
    private val SMILEY_TEXTS_OLD = arrayOf(
      "[微笑]",
      "[撇嘴]",
      "[色]",
      "[发呆]",
      "[得意]",
      "[流泪]",
      "[害羞]",
      "[闭嘴]",
      "[睡]",
      "[大哭]",
      "[尴尬]",
      "[发怒]",
      "[调皮]",
      "[呲牙]",
      "[惊讶]",
      "[难过]",
      "[酷]",
      "[冷汗]",
      "[抓狂]",
      "[吐]",
      "[偷笑]",
      "[可爱]",
      "[白眼]",
      "[傲慢]",
      "[饥饿]",
      "[困]",
      "[惊恐]",
      "[流汗]",
      "[憨笑]",
      "[悠闲]",
      "[奋斗]",
      "[咒骂]",
      "[疑问]",
      "[嘘]",
      "[晕]",
      "[疯了]",
      "[衰]",
      "[骷髅]",
      "[敲打]",
      "[再见]",
      "[擦汗]",
      "[抠鼻]",
      "[鼓掌]",
      "[糗大了]",
      "[坏笑]",
      "[左哼哼]",
      "[右哼哼]",
      "[哈欠]",
      "[鄙视]",
      "[委屈]",
      "[快哭了]",
      "[阴险]",
      "[亲亲]",
      "[吓]",
      "[可怜]",
      "[菜刀]",
      "[西瓜]",
      "[啤酒]",
      "[篮球]",
      "[乒乓]",
      "[咖啡]",
      "[饭]",
      "[猪头]",
      "[玫瑰]",
      "[凋谢]",
      "[示爱]",
      "[爱心]",
      "[心碎]",
      "[蛋糕]",
      "[闪电]",
      "[炸弹]",
      "[刀]",
      "[足球]",
      "[瓢虫]",
      "[便便]",
      "[月亮]",
      "[太阳]",
      "[礼物]",
      "[拥抱]",
      "[强]",
      "[弱]",
      "[握手]",
      "[胜利]",
      "[抱拳]",
      "[勾引]",
      "[拳头]",
      "[差劲]",
      "[爱你]",
      "[NO]",
      "[OK]"
    )
    private var uniqueInstance: EmojiHelper? = null

    /**
     * key -> SimpleEmoji 映射
     */
    private val sItemMap: MutableMap<String?, SimpleEmoji> = HashMap()

    val instance: EmojiHelper
      get() {
        if (uniqueInstance == null) {
          synchronized(EmojiHelper::class.java) {
            if (uniqueInstance == null) {
              uniqueInstance = EmojiHelper()
            }
          }
        }
        return uniqueInstance!!
      }

    val smallGroupUuid: String
      get() = MX_EMOJI_GROUP_DEFAULT_UNIQUE_UUID + "_" + userId

    fun getDrawableUri(context: Context?, drawableName: String): String {
      return "android.resource://" + context!!.packageName + "/drawable/" + drawableName
    }

    fun getDrawableId(ctx: Context?, resName: String?): Int {
      if (TextUtils.isEmpty(resName)) {
        return -1
      }
      val name = resName!!.replace("/", "").replace(":", "")
      return ctx!!.resources.getIdentifier(name, "drawable", ctx.packageName)
    }

    /**
     * 获取自定义表情包
     *
     * @return
     */
    val customGroup: EmojiGroup?
      get() {
        val userId = userId
        return if (userId < 0) {
          null
        } else DbManager.get().getCustomEmojiGroup(userId)
      }

    // 创建小表情的删除按钮
    fun createDelEmoji(ctx: Context?): Emoji {
      val emoji = Emoji()
      emoji.isDelete = true
      emoji.thumbnail = getDrawableUri(ctx, "emoji_del_btn_nor")
      emoji.key = "del_btn"
      emoji.resId = getDrawableId(ctx, "emoji_del_btn")
      return emoji
    }

    // 创建自定义表情的添加按钮
    private fun createAddEmoji(ctx: Context?): Emoji {
      val emoji = Emoji()
      emoji.description = Desc()
      emoji.isDelete = false
      emoji.thumbnail = getDrawableUri(ctx, "mx_emoji_custom_add")
      emoji.key = "add_btn"
      emoji.resId = getDrawableId(ctx, "mx_emoji_custom_add")
      return emoji
    }

    fun isCustomGroup(group: EmojiGroup?): Boolean {
      return group != null && MX_EMOJI_GROUP_TYPE_CUSTOM == group.type
    }

    fun isSmallGroup(group: EmojiGroup?): Boolean {
      return group != null && MX_EMOJI_GROUP_DEFAULT_TYPE == group.type
    }

    fun hasEmoji(sha1: String?): Boolean {
      return DbManager.get().hasCustomEmoji(sha1)
    }

    fun hasGroup(uuid: String?): Boolean {
      return DbManager.get().hasEmojiGroup(uuid)
    }

    fun updateRecentlyUsedEmojis(emojis: List<Emoji?>?) {
      val uid = userId
      if (uid == -1) {
        return
      }
      DbManager.get().updateRecentUsedEmojis(emojis, uid)
    }

    val userId: Int
      get() = 0xff

    fun mapRes(ctx: Context?, emoji: Emoji?) {
      if (emoji == null) {
        return
      }
      if (emoji.isSmall && !emoji.isAdd && !TextUtils.isEmpty(emoji.key)) {
        // 映射资源 id 到 emoji 对象中；预加载 drawable 资源
        val emojiKey = emoji.key
        val resId = getDrawableId(ctx, emojiKey)
        if (resId > 0) {
          emoji.resId = resId
          sItemMap[emojiKey] = SimpleEmoji.create(emoji)
          val dr = ContextCompat.getDrawable(ctx!!, resId)
          if (dr != null) {
            LogHelper.debug("EmojiHelper", "mapRes() preload drawable: $emojiKey")
          }
        } else {
          LogHelper.debug("EmojiHelper", "not found res: $emojiKey")
        }
      } else if (!emoji.isSmall && !TextUtils.isEmpty(emoji.thumbnail)) {
        // 预加载表情包资源, 目前只加载了缩略图，如有必要可添加加载原图的逻辑
        ImageAdapter.engine().preload(ctx, null, UrlHelper.inspectUrl(emoji.thumbnail))
      }
    }

    fun figureOutEmojiSize(emoji: Emoji?): IntArray? {
      if (emoji == null) {
        return null
      }
      val outWidth: Int
      val outHeight: Int
      val width = emoji.width
      val height = emoji.height
      if (width > MAX_EMOJI_SIZE || height > MAX_EMOJI_SIZE) {
        if (width > MAX_EMOJI_SIZE && height > MAX_EMOJI_SIZE) {
          if (width > height) {
            // w > h
            outWidth = MAX_EMOJI_SIZE
            // 缩小或放大 width * s = maxW
            outHeight = computeSize((height * MAX_EMOJI_SIZE / width.toFloat()).roundToInt())
          } else {
            // h > w
            outHeight = MAX_EMOJI_SIZE
            // 缩小或放大 height * s = maxH
            outWidth = computeSize(Math.round(width * MAX_EMOJI_SIZE / height.toFloat()))
          }
        } else if (width > MAX_EMOJI_SIZE) {
          outWidth = MAX_EMOJI_SIZE
          // 缩小或放大 width * s = maxW
          outHeight = computeSize(Math.round(height * MAX_EMOJI_SIZE / width.toFloat()))
        } else {
          outHeight = MAX_EMOJI_SIZE
          // 缩小或放大 height * s = maxH
          outWidth = computeSize(Math.round(width * MAX_EMOJI_SIZE / height.toFloat()))
        }
      } else {
        outWidth = width
        outHeight = height
      }
      return intArrayOf(outWidth, outHeight)
    }

    private fun computeSize(size: Int): Int {
      return if (size > MAX_EMOJI_SIZE) MAX_EMOJI_SIZE else size
    }
    //////////////////////////// Emoji text API ////////////////////////////
    /**
     * 返回需要删除的表情的索引
     *
     * @param source 原始文本
     * @return 如果未找到合适的位置则返回 -1
     */
    fun findDeleteIndex(source: String): Int {
      val matcher = PATTERN.matcher(source)
      var smileyKey: String? = null
      while (matcher.find()) {
        smileyKey = matcher.group()
      }
      if (smileyKey != null) {
        val end = source.substring(source.length - smileyKey.length)
        if (smileyKey == end) {
          return source.lastIndexOf(smileyKey)
        }
      }
      return -1
    }

    fun getSmileText(text: CharSequence): String? {
      if (TextUtils.isEmpty(text)) {
        return null
      }
      val sb = StringBuilder()
      val p = Pattern.compile("(\\[[^\\]]*\\])")
      val m = p.matcher(text)
      while (m.find()) {
        val smileText = m.group().substring(1, m.group().length - 1)
        val smileKey = getSmileKeyBySmileText(smileText)
        sb.append(smileKey)
      }
      return sb.toString()
    }

    private fun getSmileKeyBySmileText(smileText: String): String {
      var index = -1
      for (i in SMILEY_TEXTS.indices) {
        if (SMILEY_TEXTS[i].contains(smileText)) {
          index = i
          break
        }
      }
      return SMILEY_KEYS[index].replace("\\", "")
    }

    fun getShowText(text: CharSequence): String {
      if (TextUtils.isEmpty(text)) {
        return ""
      }
      val sb = StringBuilder()
      val p = Pattern.compile("(\\[[^\\]]*\\])")
      val m = p.matcher(text)
      while (m.find()) {
        sb.append("[")
        sb.append(m.group(), 1, m.group().length - 1)
        sb.append("]")
      }
      return sb.toString()
    }

    /**
     * 过滤表情(表情转为[大笑]这种文本格式)
     */
    fun replaceSmileyCodeWithText(orgText: CharSequence): String {
      val matcher = PATTERN.matcher(orgText)
      var out = orgText.toString()
      while (matcher.find()) {
        val emoji = sItemMap[matcher.group()]
        if (emoji?.text == null) {
          continue
        }
        out = orgText.replace(emoji.replaceKey!!.toRegex(), emoji.text)
      }
      return out
    }

    /**
     * 判断一个文本中是否包含表情
     *
     * @param text 文本
     * @return true 表示包含， false 表示不包含
     */
    fun isContainSmiley(text: CharSequence): Boolean {
      val matcher = PATTERN.matcher(text)
      while (matcher.find()) {
        val key = matcher.group()
        val emoji = sItemMap[key]
        if (emoji != null) {
          return true
        }
      }
      return false
    }
    //////////////////////////// Span or Bitmap API ////////////////////////////
    /**
     * 粘贴文本到输入框，同时将表情符号转换成 ImageSpan
     *
     * @param view EditText
     */
    fun pasteToEditText(view: EditText, pasteString: String, maxLength: Int) {
      var realMax = maxLength
      if (realMax < 0) {
        realMax = 1000
      }
      val matcher = PATTERN.matcher(pasteString)
      var smileyKey: String?
      var start = 0
      var smileyStart: Int
      while (matcher.find()) {
        smileyKey = matcher.group()
        smileyStart = matcher.start()
        if (smileyStart > start) {
          val pasteTextBefore = pasteString.substring(start, smileyStart)
          val index = view.selectionStart
          view.text.insert(index, pasteTextBefore)
          val selection = index + pasteTextBefore.length
          view.setSelection(if (selection > realMax) realMax else selection)
        }
        val text = toSpannable(view.context, smileyKey, view.textSize)
        val index = view.selectionStart
        view.text.insert(index, text)
        val selection = index + text.length
        view.setSelection(if (selection > realMax) realMax else selection)
        start = matcher.end()
        if (selection > realMax) {
          return
        }
      }
      val index = view.selectionStart
      var pasteTextAfter = pasteString.substring(start)
      if (realMax > 0 && index + pasteTextAfter.length > realMax) {
        if (realMax <= view.text.length) {
          return
        }
        pasteTextAfter = pasteTextAfter.substring(0, realMax - view.text.length)
      }
      view.text.insert(index, pasteTextAfter)
      view.setSelection(index + pasteTextAfter.length)
    }

    /**
     * 将源字符串中的表情符号转换成 ImageSpan 并返回 Spannable 对象
     */
    fun toSpannable(
      context: Context, source: CharSequence?,
      textSize: Float
    ): Spannable {
      return toSpannable(context, source, 1f, textSize, true)
    }

    /**
     * 将源字符串中的表情符号转换成 ImageSpan 并返回 Spannable 对象
     */
    fun toSpannable(
      context: Context, source: CharSequence?,
      scale: Float, textSize: Float
    ): Spannable {
      return toSpannable(context, source, scale, textSize, true)
    }

    /**
     * 将源字符串中的表情符号转换成 ImageSpan 并返回 SpannableString 对象
     *
     * @param scale       缩放比例
     * @param textSize    文本大小，自动优化时会根据文本大小自动调整表情大小
     * @param smartAdjust 是否自动优化表情大小
     */
    private fun toSpannable(
      context: Context, source: CharSequence?,
      scale: Float, textSize: Float,
      smartAdjust: Boolean
    ): Spannable {
      val result = prefillAtSpan(source)
      val emojiSize = scale * textSize * if (smartAdjust) 1.3f else 1f
      val matcher = PATTERN.matcher(result)
      return try {
        while (matcher.find()) {
          val key = matcher.group()
          val start = matcher.start()
          val end = start + key.length
          // 通过图片资源id来得到bitmap，用一个ImageSpan来包装
          val what: Any = createSpan(context, key, emojiSize.toInt())
          // 计算该图片名字的长度，也就是要替换的字符串的长度
          // 将该图片替换字符串中规定的位置中
          result.setSpan(what, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        result
      } catch (e: Throwable) {
        result
      }
    }

    private fun createSpan(context: Context, key: String, size: Int): ImageSpan {
      val emoji = sItemMap[key]
      val dr = ContextCompat.getDrawable(context, emoji!!.resId)
      (dr as? BitmapDrawable)?.setBounds(0, 0, size, size)
      return EmojiSpan(dr, emoji.key)
    }

    private fun prefillAtSpan(source: CharSequence?): Spannable {
      // 保留 source 中已有的 span
      val result: Spannable = SpannableString.valueOf(source ?: "")
      // 取出要处理的文本
      val text = result.toString()
      // 倒叙遍历会导致点击表情的删除按钮时将所有的 @someone 都被删除，所以改成正序遍历
      var st = 0
      val len = text.length
      while (st < len) {
        if (text[st] == '@') {
          for (en in st until len) {
            if (text[en] == '\u2005') {
              result.setSpan(
                CustomAtSpan(text.substring(st, en + 1)),
                st, en + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
              )
              break
            }
          }
        }
        st++
      }
      return result
    }

    private fun smileyToBitmap(context: Context, resId: Int, size: Float): Bitmap? {
      val dr = ContextCompat.getDrawable(context, resId)
      if (dr is BitmapDrawable) {
        dr.setBounds(0, 0, size.toInt(), size.toInt())
        return dr.bitmap
      }
      return null
    }

    private fun smileyToBitmap(context: Context, resId: Int): Bitmap? {
      val size = context.resources.getDimensionPixelSize(R.dimen.mx_dp_35)
      return smileyToBitmap(context, resId, size.toFloat())
    }

    //////////////////////////// JS API ////////////////////////////
    fun convertRichText(context: Context, text: CharSequence): String {
      return if (!isContainSmiley(text)) {
        text.toString()
      } else {
        val smileyList = findSmiley(text)
        var result = text
        for (emoji in smileyList) {
          // 将文本中的表情替换成 img 标签
          result = text.toString().replace(emoji.key!!, smileyToImgTag(context, emoji.resId))
        }
        result.toString()
      }
    }

    private fun findSmiley(text: CharSequence): List<SimpleEmoji> {
      val matcher = PATTERN.matcher(text)
      var emoji: SimpleEmoji?
      // 使用 HashSet 过滤掉重复的元素
      val smileySet: MutableSet<SimpleEmoji> = HashSet()
      while (matcher.find()) {
        val key = matcher.group()
        emoji = sItemMap[key]
        if (emoji != null) {
          smileySet.add(emoji)
        }
      }
      return ArrayList(smileySet)
    }

    /**
     * 将表情转换 img 标签
     *
     * @param context [Context]
     * @param resId   表情 resId
     * @return &lt;img src='data:image/jpeg;base64,base64_src_data'/&gt; 格式的 img 标签
     */
    private fun smileyToImgTag(context: Context, resId: Int): String {
      val bitmap = smileyToBitmap(context, resId) ?: return ""
      val bos = ByteArrayOutputStream()
      // 参数100表示不压缩 0表示压缩到最小
      bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos)
      val base64Image = Base64.encodeToString(bos.toByteArray(), Base64.DEFAULT)
      return "<img src='data:image/jpeg;base64,$base64Image'/>"
    }

    init {
      MAX_EMOJI_SIZE = (300 / 2 * Resources.getSystem().displayMetrics.density).toInt()
    }

    private const val TAG = "EmojiHelper"
  }

  /**
   * 更新表情包列表(未确定更新时机，主要同步表情包的名称和thumbnail)
   *
   * @param context
   */
  fun initEmojiGroupList(context: Context?) {
    Log.d(TAG, "initEmojiGroupList() called with: context = $context")
    // 从server获取数据
    val groups: List<EmojiGroup> = ArrayList()
    // 因为这里本就在子线程 所以直接同步更新数据即可
    for (group in groups) {
      //
    }
  }

  private fun updateEmojiGroupList(context: Context, emojiGroups: List<EmojiGroup>?) {
    Log.d(TAG, "updateEmojiGroupList() called with: context = $context, emojiGroups = $emojiGroups")
    val userId = userId
    if (emojiGroups == null || emojiGroups.isEmpty() || userId < 0) {
      return
    }
    val manager: DbManager = DbManager.get()
    for (group in emojiGroups) {
      if (isSmallGroup(group)) {
        continue
      }
      // 是否需要更新本地表情
      // case 1: updatedAt 字段不一致（是否取最新的时间？）
      // case 2: 本地无该分组数据
      val needUpdate: Boolean
      val newComing: Boolean
      val localGroup = manager.queryEmojiGroupByUuid(group.uuid, userId)
      if (localGroup == null) {
        newComing = true
        needUpdate = false
        manager.insertEmojiGroup(group, userId)
      } else {
        newComing = false
        // 这个判断是否准确？
        needUpdate = group.updatedAt != localGroup.updatedAt
        manager.updateEmojiGroupNameThumbnailCount(group, userId)
      }
      if (!needUpdate && !newComing) {
        continue
      }
    }
  }

  /**
   * 获取当前用户的表情包列表：按照小表情，自定义表情包，预置表情包，商店表情包顺序
   *
   * @param mContext
   * @return
   */
  fun getCurrentEmojiGroupList(mContext: Context?): List<EmojiGroup> {
    val list = DbManager.get().queryAllEmojiGroup(userId)
    val sortList = mutableListOf<EmojiGroup>()
    if (list != null && list.isNotEmpty()) {
      val map = mutableMapOf<Int, EmojiGroup>()
      for (group in list) {
        if (TextUtils.isEmpty(group.thumbnail)) {
          group.thumbnail = getDrawableUri(mContext, "mx_input_emoji_custom")
        }
        when (group.type) {
          MX_EMOJI_GROUP_TYPE_STORE -> sortList.add(group)
          else -> map[group.type] = group
        }
      }
      //预置表情包
      if (map.containsKey(MX_EMOJI_GROUP_TYPE_DEFAULT)) {
        sortList.add(0, map[MX_EMOJI_GROUP_TYPE_DEFAULT]!!)
      }
      //自定义表情
      if (map.containsKey(MX_EMOJI_GROUP_TYPE_CUSTOM)) {
        sortList.add(0, map[MX_EMOJI_GROUP_TYPE_CUSTOM]!!)
      }
      //小表情
      if (map.containsKey(MX_EMOJI_GROUP_DEFAULT_TYPE)) {
        sortList.add(0, map[MX_EMOJI_GROUP_DEFAULT_TYPE]!!)
      }
    }
    return sortList
  }

  /**
   * 初始化默认表情:小表情
   *
   * @param context
   */
  fun initDefaultEmoji(context: Context?) {
    val userId = userId
    if (userId < 0) {
      return
    }
    val packUuid = MX_EMOJI_GROUP_DEFAULT_UNIQUE_UUID + "_" + userId
    /* 小表情 */
    val smallEmojis: MutableList<Emoji> = ArrayList()
    for (i in SMILEY_KEYS.indices) {
      val emoji = Emoji()
      emoji.uuid = MX_EMOJI_DEFAULT_UNIQUE_UUID + "_" + userId + "_" + i
      emoji.sha1 = MX_EMOJI_DEFAULT_UNIQUE_UUID + "_" + userId + "_" + i
      val description = Desc()
      description.cn = SMILEY_TEXTS[i]
      emoji.description = description
      emoji.thumbnail = getDrawableUri(context, MX_EMOJI_DEFAULT_DRAWABLE_PREFIX + (i + 1))
      emoji.packUuid = packUuid
      emoji.sortOrder = i
      emoji.key = SMILEY_KEYS[i].replace("\\", "")
      emoji.replaceKey = SMILEY_KEYS[i]
      smallEmojis.add(emoji)
    }
    DbManager.get().insertEmojiList(smallEmojis, userId)

    /* 小表情分组 */
    val smallGroup = EmojiGroup()
    smallGroup.uuid = packUuid
    smallGroup.sha1 = packUuid
    smallGroup.type = MX_EMOJI_GROUP_DEFAULT_TYPE
    smallGroup.thumbnail = getDrawableUri(context, "mx_input_smile_grey")
    smallGroup.count = smallEmojis.size
    DbManager.get().insertEmojiGroup(smallGroup, userId)
  }

  /**
   * 获取自定义表情包的uuid
   *
   * @return
   */
  val customEmojiGroupUuid: String?
    get() {
      val group = DbManager.get().queryEmojiGroupByType(MX_EMOJI_GROUP_TYPE_CUSTOM, userId)
      return group?.uuid
    }

  /**
   * 自定义表情列表
   *
   * @return
   */
  val customEmojiList: List<Emoji>?
    get() {
      val userId = userId
      return if (userId < 0) {
        null
      } else DbManager.get().queryCustomEmojis(userId)
    }

  fun getCustomEmojiFromRaw(context: Context, rawId: Int): String? {
    return try {
      val `is` = context.resources.openRawResource(rawId)
      `is`.source().buffer().readUtf8()
    } catch (e: Exception) {
      null
    }
  }

  fun getFromAssets(context: Context, fileName: String?): String? {
    return try {
      val `is` = context.assets.open(fileName!!)
      `is`.source().buffer().readUtf8()
    } catch (e: Exception) {
      null
    }
  }

  fun getEmojiList(context: Context?, group: EmojiGroup?): List<Emoji>? {
    val uid = userId
    if (uid == -1) {
      return null
    }
    val emojis: MutableList<Emoji> = ArrayList()
    if (isCustomGroup(group)) {
      // 自定义表情第一个是添加按钮
      emojis.add(0, createAddEmoji(context))
      val result = DbManager.get().queryCustomEmojis(uid)
      if (result != null) {
        emojis.addAll(result)
      }
    } else {
      val result = DbManager.get()
        .queryEmojiListByGroupUuid(group!!.uuid, uid)
      if (result != null) {
        emojis.addAll(result)
      }
    }
    return emojis
  }

  val recentlyUsedEmojis: List<Emoji>?
    get() {
      val uid = userId
      return if (uid == -1) {
        null
      } else DbManager.get().queryRecentUsedEmojis(uid)
    }

  /**
   * 自定义表情排序（排序成功后直接将数据库原有的表情清空，再将新顺序的数据添加）
   *
   * @param moveList
   * @param data
   */
  fun moveToFirst(moveList: List<Emoji>?, data: MutableList<Emoji>?): List<Emoji>? {
    if (moveList == null || moveList.isEmpty()) {
      return data
    }
    if (data == null || data.isEmpty()) {
      return data
    }
    for (move in moveList) {
      DATA@ for (emoji in data) {
        if (instance.isEqual(move, emoji)) {
          data.remove(emoji)
          break@DATA
        }
      }
    }
    for (j in moveList.indices.reversed()) {
      val move = moveList[j]
      data.add(0, move)
    }

    //更新数据库
    cleanUpAndUpdateCustomEmoji(data)
    //        DBStoreHelper.getInstance(context).updateEmojiSortOrder(currentUserID, data);
    return data
  }

  /**
   * 删除自定义表情
   *
   * @param currentUserID
   * @param deleteList
   * @param data
   * @return
   */
  fun deleteEmojis(currentUserID: Int, deleteList: List<Emoji>?, data: MutableList<Emoji>?): List<Emoji>? {
    if (deleteList == null || deleteList.isEmpty()) {
      return data
    }
    if (data == null || data.isEmpty()) {
      return data
    }
    for (move in deleteList) {
      DATA@ for (emoji in data) {
        if (instance.isEqual(move, emoji)) {
          data.remove(emoji)
          break@DATA
        }
      }
    }

    //删除数据库中的数据
    DbManager.get().deleteEmojiList(currentUserID, deleteList)
    return data
  }

  /**
   * 清空并更新自定义表情：将库中的自定义表情清空，添加新的表情【注意此处添加的表情按照list倒序添加】
   */
  fun cleanUpAndUpdateCustomEmoji(data: List<Emoji>) {
    val deleteList = customEmojiList
    //清空自定义表情
    DbManager.get().deleteEmojiList(userId, deleteList)
    //倒序添加
    val newList: MutableList<Emoji> = ArrayList()
    for (j in data.indices.reversed()) {
      newList.add(data[j])
    }
    if (!newList.isEmpty()) {
      //重新添加自定义表情
      DbManager.get().insertEmojiList(newList, userId)
    }
  }

  /**
   * 判断两个表情是否是同一个表情（通过uuid判断）
   *
   * @param emoji1
   * @param emoji2
   * @return
   */
  fun isEqual(emoji1: Emoji?, emoji2: Emoji?): Boolean {
    return emoji1 != null && emoji2 != null && !TextUtils.isEmpty(emoji1.uuid) && emoji1.uuid == emoji2.uuid
  }

  /////////////// internal api ///////////////
  private class SimpleEmoji private constructor(src: Emoji) {
    val key: String? = src.key
    val replaceKey: String? = src.replaceKey
    val text: String? = src.description?.cn
    val resId: Int = src.resId

    companion object {
      fun create(src: Emoji): SimpleEmoji {
        return SimpleEmoji(src)
      }
    }
  }
}