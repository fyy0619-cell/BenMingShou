package io.github.fyy0619cell.deskpet.ui.settings

/** Where each vendor hides "let this app keep running". Paths drift between OS versions. */
object KeepAlive {
    class Brand(val name: String, val manufacturers: List<String>, val steps: String)

    private val brands = listOf(
        Brand(
            "小米 / 红米", listOf("xiaomi", "redmi", "poco"),
            "设置 → 应用设置 → 应用管理 → 本命兽\n· 省电策略：选「无限制」\n· 打开「自启动」",
        ),
        Brand(
            "华为 / 荣耀", listOf("huawei", "honor"),
            "设置 → 应用和服务 → 应用启动管理 → 本命兽\n· 关闭「自动管理」\n· 打开「允许自启动」「允许后台活动」",
        ),
        Brand(
            "OPPO / 一加 / realme", listOf("oppo", "oneplus", "realme"),
            "设置 → 电池 → 应用耗电管理 → 本命兽\n· 打开「允许完全后台行为」「允许自启动」",
        ),
        Brand(
            "vivo / iQOO", listOf("vivo", "iqoo"),
            "设置 → 电池 → 后台耗电管理 → 本命兽：选「允许后台高耗电」\n设置 → 应用与权限 → 权限管理 → 自启动：打开本命兽",
        ),
        Brand(
            "三星", listOf("samsung"),
            "设置 → 电池 → 后台使用限制 → 「从不休眠的应用」→ 添加本命兽",
        ),
        Brand(
            "其他手机", emptyList(),
            "设置 → 应用 → 本命兽 → 电池：选「不限制」或「无限制」",
        ),
    )

    /** All brands, with the one matching this phone first. */
    fun ordered(manufacturer: String): List<Brand> {
        val m = manufacturer.lowercase()
        val mine = brands.firstOrNull { b -> b.manufacturers.any { m.contains(it) } }
        return if (mine == null) brands else listOf(mine) + (brands - mine)
    }

    fun matches(b: Brand, manufacturer: String) = b.manufacturers.any { manufacturer.lowercase().contains(it) }
}
