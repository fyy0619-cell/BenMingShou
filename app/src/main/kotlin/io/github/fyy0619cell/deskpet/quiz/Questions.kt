package io.github.fyy0619cell.deskpet.quiz

import io.github.fyy0619cell.deskpet.species.Habitat

/** Deltas on the six trait axes: energy, social, bold, curious, night, ancient. */
class Option(val text: String, val delta: IntArray, val habitat: Habitat? = null)

class Question(val text: String, val options: List<Option>)

private fun o(text: String, e: Int = 0, s: Int = 0, b: Int = 0, c: Int = 0, n: Int = 0, a: Int = 0, h: Habitat? = null) =
    Option(text, intArrayOf(e, s, b, c, n, a), h)

/**
 * Each question probes two axes; its four options are the four sign combinations,
 * so no answer is "better" than another.
 */
object Questions {
    val all: List<Question> = listOf(
        Question(
            "周末突然空出一整天，你会？",
            listOf(
                o("约上一群朋友出去疯玩", e = 1, s = 1),
                o("一个人去骑车爬山，走得越远越好", e = 1, s = -1),
                o("叫两三个好友来家里慢慢聊", e = -1, s = 1),
                o("关掉手机，睡到自然醒", e = -1, s = -1),
            ),
        ),
        Question(
            "路上看见一条从没走过的小巷，你会？",
            listOf(
                o("直接钻进去，看看通向哪里", b = 1, c = 1),
                o("想进去又有点怕，先在巷口张望", b = -1, c = 1),
                o("没兴趣，但真要走也不怕", b = 1, c = -1),
                o("绕开它，还是熟悉的路安心", b = -1, c = -1),
            ),
        ),
        Question(
            "一天里，你状态最好的时候是？",
            listOf(
                o("清晨，天一亮就精神十足", e = 1, n = -2),
                o("午后，晒着太阳最舒服", e = -1, n = -1),
                o("傍晚，热闹才刚刚开始", e = 1, n = 1),
                o("深夜，世界安静了我才醒过来", e = -1, n = 2),
            ),
        ),
        Question(
            "如果能穿越一次，你想去？",
            listOf(
                o("恐龙时代，亲眼看看霸王龙", c = 1, a = 2),
                o("冰河时代，在雪原上安静地待一会儿", c = -1, a = 1),
                o("一百年后，看看未来长什么样", c = 1, a = -2),
                o("哪也不去，现在就挺好", c = -1, a = -1),
            ),
        ),
        Question(
            "让你选一个地方住一年，你选？",
            listOf(
                o("海边的小屋，每天听着浪声", h = Habitat.WATER),
                o("山顶的观星台，抬头就是天空", h = Habitat.AIR),
                o("森林深处，没人打扰", s = -1, h = Habitat.LAND),
                o("热闹的小镇，楼下就是集市", s = 1, h = Habitat.LAND),
            ),
        ),
        Question(
            "朋友遇到麻烦来找你，你会？",
            listOf(
                o("二话不说冲上去帮忙", b = 1, s = 1),
                o("叫上大家一起想办法", b = -1, s = 1),
                o("不声不响，一个人把事办妥", b = 1, s = -1),
                o("先默默陪着，等他需要再开口", b = -1, s = -1),
            ),
        ),
    )

    val combinations: Int = all.fold(1) { acc, q -> acc * q.options.size }
}
