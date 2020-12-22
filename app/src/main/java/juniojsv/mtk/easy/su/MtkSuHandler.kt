package juniojsv.mtk.easy.su

import android.content.Context
import android.content.SharedPreferences
import android.os.AsyncTask
import java.io.File
import java.io.FileOutputStream

class MtkSuHandler(
    private val context: Context,
    private val onFinished: (result: Boolean, log: String) -> Unit
) :
    AsyncTask<Void, Void, String>() {

    private val preferences: SharedPreferences =
        context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)

    override fun onPreExecute() {
        super.onPreExecute()
        "Please wait".toast(context, true)
    }

    override fun doInBackground(vararg args: Void?): String {
        with(context) {
            if (filesDir.listFiles()?.isEmpty() == true) {
                File(filesDir, "64").mkdir()
                File(filesDir, "32").mkdir()
                assets?.list("")?.forEach { name ->
                    when (name) {
                        "magisk-boot.sh", "magiskinit32", "magiskinit64", "mtk-su32", "mtk-su64" -> {
                            val file: File =
                                when {
                                    name.endsWith("32") -> File(
                                        File("$filesDir/32"),
                                        name.removeSuffix("32")
                                    )
                                    name.endsWith("64") -> File(
                                        File("$filesDir/64"),
                                        name.removeSuffix("64")
                                    )
                                    else -> File(filesDir, name)
                                }
                            val output = FileOutputStream(file)
                            assets.open(name).copyTo(output, 512)
                            file.setExecutable(true, true)
                            output.close()
                        }
                    }
                }
            }
        }

        val runAs64 = preferences.getBoolean("run_as_64", false)
        return Runtime.getRuntime()
            .exec("sh ${context.filesDir}/magisk-boot.sh ${if (runAs64) "64" else "32"}")
            .getOutput()
    }

    override fun onPostExecute(result: String) {
        super.onPostExecute(result)
        onFinished(File("/sbin/su").exists(), result)
    }

}