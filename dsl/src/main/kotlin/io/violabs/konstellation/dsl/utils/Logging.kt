package io.violabs.konstellation.dsl.utils

internal object Colors {
    const val RESET = "\u001B[0m"
    const val RED = "\u001B[31m"
    const val GREEN = "\u001B[32m"
    const val YELLOW = "\u001B[33m"
    const val BLUE = "\u001B[34m"
    const val PURPLE = "\u001B[35m"
    const val CYAN = "\u001B[36m"

    fun red(content: String): String = "$RED$content$RESET"
    fun green(content: String): String = "$GREEN$content$RESET"
    fun yellow(content: String): String = "$YELLOW$content$RESET"
    fun blue(content: String): String = "$BLUE$content$RESET"
    fun purple(content: String): String = "$PURPLE$content$RESET"
    fun cyan(content: String): String = "$CYAN$content$RESET"
}

internal object Logging {
    const val LOGO = "${Colors.GREEN}konstellation${Colors.RESET}"
    const val DELIMITER = "${Colors.PURPLE}*${Colors.CYAN}>>${Colors.RESET}"
    const val ID_TEMPLATE = "${Colors.CYAN}[${Colors.RESET}%-30s${Colors.CYAN}]${Colors.RESET}"
    const val INFO = "${Colors.CYAN}INFO ${Colors.RESET}"
    const val WARN = "${Colors.YELLOW}WARN ${Colors.RESET}"
    const val DEBUG = "${Colors.BLUE}DEBUG${Colors.RESET}"
    const val ERROR = "${Colors.RED}ERROR${Colors.RESET}"

    const val CMD = "CMD"
    const val TUTORIAL = "TUTORIAL"
}

private const val MAX_NAME_LENGTH = 30
private const val MAX_TAKE_LENGTH = MAX_NAME_LENGTH - 3 // For "..."

@Suppress("TooManyFunctions")
data class Logger(
    private val logId: String,
    private var isDebugEnabled: Boolean = false,
    private var isWarningEnabled: Boolean = false
) {
    private val activeBranches = mutableSetOf<Int>()


    // Simplified - just truncate if too long, no fancy padding
    private val formattedName: String = when {
        logId.length > MAX_NAME_LENGTH -> logId.take(MAX_TAKE_LENGTH) + "..."
        else -> logId
    }

    fun enableDebug(): Logger = apply {
        isDebugEnabled = true
    }

    fun disableDebug(): Logger = apply {
        isDebugEnabled = false
    }

    fun debugEnabled(): Boolean = isDebugEnabled

    fun globalDebugEnabled(): Boolean = System.getProperty("debug")?.toBoolean() ?: false

    fun disableWarning(): Logger = apply {
        isWarningEnabled = false
    }

    private fun tierPrefix(tier: Int): String {
        if (tier <= 0) return ""

        val sb = StringBuilder()
        for (i in 1 until tier) {
            sb.append(if (activeBranches.contains(i)) "|  " else "   ")
        }
        sb.append("|__ ")

        return sb.toString()
    }

    private fun updateBranches(tier: Int, branch: Boolean) {
        activeBranches.removeIf { it >= tier }
        if (branch) activeBranches.add(tier)
    }

    fun info(message: Any, tier: Int = 0, branch: Boolean = false) {
        val id = Logging.ID_TEMPLATE.format(formattedName)
        val prefix = tierPrefix(tier)
        println("${Logging.LOGO} ${Logging.INFO} $id ${Logging.DELIMITER} $prefix$message")
        updateBranches(tier, branch)
    }

    fun debug(message: Any, tier: Int = 0, branch: Boolean = false) {
        if (!isDebugEnabled) return
        val id = Logging.ID_TEMPLATE.format(formattedName)
        val prefix = tierPrefix(tier)
        println("${Logging.LOGO} ${Logging.DEBUG} $id ${Logging.DELIMITER} $prefix$message")
        updateBranches(tier, branch)
    }

    fun warn(message: Any, tier: Int = 0, branch: Boolean = false) {
        if (!isWarningEnabled) return
        val id = Logging.ID_TEMPLATE.format(formattedName)
        val prefix = tierPrefix(tier)
        println(
            "${Logging.LOGO} ${Logging.WARN} $id ${Logging.DELIMITER} ${Colors.YELLOW}$prefix$message${Colors.RESET}"
        )
        updateBranches(tier, branch)
    }

    fun error(message: Any, tier: Int = 0, branch: Boolean = false) {
        val id = Logging.ID_TEMPLATE.format(formattedName)
        val prefix = tierPrefix(tier)
        println("${Logging.LOGO} ${Logging.ERROR} $id ${Logging.DELIMITER} ${Colors.RED}$prefix$message${Colors.RESET}")
        updateBranches(tier, branch)
    }

    // For multi-line logging with consistent indentation
    fun infoMultiline(message: String) {
        val lines = message.split("\n")

        if (lines.isEmpty()) return

        // Log the first line normally
        info(lines.first())

        // For subsequent lines, maintain proper indentation
        val id = Logging.ID_TEMPLATE.format(formattedName)
        val prefix = "${Logging.LOGO} ${Logging.INFO} $id ${Logging.DELIMITER}   | "

        lines.drop(1).forEach { line ->
            println("$prefix$line")
        }
    }
}

private val LOG_MAP = mutableMapOf<String, Logger>()
private var DEBUG_ENABLED = System.getProperty("debug")?.toBoolean() ?: false
private val WARNING_ENABLED = System.getProperty("warn")?.toBoolean() ?: true

interface VLoggable {
    fun logId(): String? = null
    val logger: Logger
        get() {
            val name = logId() ?: this::class.simpleName ?: "DefaultLogger"

            return LOG_MAP.getOrPut(name) {
                val logger = Logger(name)
                if (DEBUG_ENABLED) logger.enableDebug()
                if (!WARNING_ENABLED) logger.disableWarning()
                logger
            }
        }

    companion object {
        fun setGlobalDebug(enabled: Boolean) {
            DEBUG_ENABLED = enabled
            LOG_MAP.values.forEach { logger ->
                if (enabled) logger.enableDebug() else logger.disableDebug()
            }
        }

        fun resetGlobalDebug() {
            setGlobalDebug(System.getProperty("debug")?.toBoolean() ?: false)
        }
    }
}
