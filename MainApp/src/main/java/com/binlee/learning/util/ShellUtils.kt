package com.binlee.learning.util

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

/**
 * <pre>
 * author: Blankj
 * blog  : http://blankj.com
 * time  : 2016/08/07
 * desc  : utils about shell
</pre> *
 */
class ShellUtils private constructor() {
  /**
   * The result of command.
   */
  class CommandResult(var result: Int, var successMsg: String?, var errorMsg: String?)

  companion object {
    private val LINE_SEP = System.getProperty("line.separator")

    /**
     * Execute the command.
     *
     * @param command  The command.
     * @param isRooted True to use root, false otherwise.
     * @return the single [CommandResult] instance
     */
    fun execCmd(command: String, isRooted: Boolean): CommandResult {
      return execCmd(arrayOf(command), isRooted, true)
    }
    /**
     * Execute the command.
     *
     * @param commands        The commands.
     * @param isRooted        True to use root, false otherwise.
     * @param isNeedResultMsg True to return the message of result, false otherwise.
     * @return the single [CommandResult] instance
     */
    /**
     * Execute the command.
     *
     * @param commands The commands.
     * @param isRooted True to use root, false otherwise.
     * @return the single [CommandResult] instance
     */
    @JvmOverloads
    fun execCmd(commands: Array<String>?, isRooted: Boolean, isNeedResultMsg: Boolean = true)
        : CommandResult {
      var result = -1
      if (commands == null || commands.isEmpty()) {
        return CommandResult(result, null, null)
      }
      var process: Process? = null
      var successResult: BufferedReader? = null
      var errorResult: BufferedReader? = null
      var successMsg: StringBuilder? = null
      var errorMsg: StringBuilder? = null
      var os: DataOutputStream? = null
      try {
        process = Runtime.getRuntime().exec(if (isRooted) "su" else "sh")
        os = DataOutputStream(process.outputStream)
        for (command in commands) {
          os.write(command.toByteArray())
          os.writeBytes(LINE_SEP)
          os.flush()
        }
        os.writeBytes("exit$LINE_SEP")
        os.flush()
        result = process.waitFor()
        if (isNeedResultMsg) {
          successMsg = StringBuilder()
          errorMsg = StringBuilder()
          successResult = BufferedReader(
            InputStreamReader(
              process.inputStream,
              StandardCharsets.UTF_8
            )
          )
          errorResult = BufferedReader(
            InputStreamReader(
              process.errorStream,
              StandardCharsets.UTF_8
            )
          )
          var line: String?
          if (successResult.readLine().also { line = it } != null) {
            successMsg.append(line)
            while (successResult.readLine().also { line = it } != null) {
              successMsg.append(LINE_SEP).append(line)
            }
          }
          if (errorResult.readLine().also { line = it } != null) {
            errorMsg.append(line)
            while (errorResult.readLine().also { line = it } != null) {
              errorMsg.append(LINE_SEP).append(line)
            }
          }
        }
      } catch (e: Exception) {
        e.printStackTrace()
      } finally {
        try {
          os?.close()
        } catch (e: IOException) {
          e.printStackTrace()
        }
        try {
          successResult?.close()
        } catch (e: IOException) {
          e.printStackTrace()
        }
        try {
          errorResult?.close()
        } catch (e: IOException) {
          e.printStackTrace()
        }
        process?.destroy()
      }
      return CommandResult(
        result,
        successMsg?.toString(),
        errorMsg?.toString()
      )
    }

    /**
     * Execute the command.
     *
     * @param commands The commands.
     * @param isRooted True to use root, false otherwise.
     * @return the single [CommandResult] instance
     */
    fun execCmd(commands: List<String?>?, isRooted: Boolean): CommandResult {
      return execCmd(commands?.toTypedArray() as Array<String>, isRooted, true)
    }

    /**
     * Execute the command.
     *
     * @param command         The command.
     * @param isRooted        True to use root, false otherwise.
     * @param isNeedResultMsg True to return the message of result, false otherwise.
     * @return the single [CommandResult] instance
     */
    private fun execCmd(command: String, isRooted: Boolean, isNeedResultMsg: Boolean)
        : CommandResult {
      return execCmd(arrayOf(command), isRooted, isNeedResultMsg)
    }

    /**
     * Execute the command.
     *
     * @param commands        The commands.
     * @param isRooted        True to use root, false otherwise.
     * @param isNeedResultMsg True to return the message of result, false otherwise.
     * @return the single [CommandResult] instance
     */
    private fun execCmd(commands: List<String?>?, isRooted: Boolean, isNeedResultMsg: Boolean)
        : CommandResult {
      return execCmd(commands?.toTypedArray() as Array<String>, isRooted, isNeedResultMsg)
    }
  }
}